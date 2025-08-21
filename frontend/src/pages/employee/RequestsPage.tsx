import React, { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { format, parseISO } from 'date-fns';
import { ja } from 'date-fns/locale';
import { 
  PlusIcon, 
  DocumentTextIcon, 
  TrashIcon
} from '@heroicons/react/24/outline';

import { 
  Button, 
  Card, 
  CardContent,
  Input,
  Modal,
  Badge,
  LoadingSpinner
} from '../../components/ui';
import { requestService } from '../../services/requestService';
import type { LeaveRequest, TimeModificationRequest } from '../../types';

const leaveRequestSchema = z.object({
  type: z.enum(['paid_leave', 'sick_leave', 'personal_leave']),
  startDate: z.string().min(1, '開始日を選択してください'),
  endDate: z.string().min(1, '終了日を選択してください'),
  reason: z.string().min(1, '理由を入力してください'),
});

const timeModificationSchema = z.object({
  date: z.string().min(1, '日付を選択してください'),
  requestedClockIn: z.string().optional(),
  requestedClockOut: z.string().optional(),
  reason: z.string().min(1, '理由を入力してください'),
});

type LeaveRequestFormData = z.infer<typeof leaveRequestSchema>;
type TimeModificationFormData = z.infer<typeof timeModificationSchema>;

const RequestsPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'leave' | 'time'>('leave');
  const [leaveRequests, setLeaveRequests] = useState<LeaveRequest[]>([]);
  const [timeRequests, setTimeRequests] = useState<TimeModificationRequest[]>([]);
  const [showLeaveModal, setShowLeaveModal] = useState(false);
  const [showTimeModal, setShowTimeModal] = useState(false);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  const leaveForm = useForm<LeaveRequestFormData>({
    resolver: zodResolver(leaveRequestSchema),
  });

  const timeForm = useForm<TimeModificationFormData>({
    resolver: zodResolver(timeModificationSchema),
  });

  useEffect(() => {
    loadRequests();
  }, []);

  const loadRequests = async () => {
    setLoading(true);
    try {
      const [leaveResponse, timeResponse] = await Promise.all([
        requestService.getLeaveRequests(),
        requestService.getTimeModificationRequests(),
      ]);

      if (leaveResponse.success && leaveResponse.data) {
        setLeaveRequests(leaveResponse.data);
      }
      if (timeResponse.success && timeResponse.data) {
        setTimeRequests(timeResponse.data);
      }
    } catch (error) {
      console.error('Failed to load requests:', error);
    } finally {
      setLoading(false);
    }
  };

  const onSubmitLeaveRequest = async (data: LeaveRequestFormData) => {
    setSubmitting(true);
    try {
      const response = await requestService.createLeaveRequest(data);
      if (response.success) {
        setShowLeaveModal(false);
        leaveForm.reset();
        loadRequests();
      }
    } catch (error) {
      console.error('Failed to submit leave request:', error);
    } finally {
      setSubmitting(false);
    }
  };

  const onSubmitTimeModification = async (data: TimeModificationFormData) => {
    setSubmitting(true);
    try {
      const response = await requestService.createTimeModification(data);
      if (response.success) {
        setShowTimeModal(false);
        timeForm.reset();
        loadRequests();
      }
    } catch (error) {
      console.error('Failed to submit time modification:', error);
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeleteRequest = async (type: 'leave' | 'time', id: string) => {
    if (!confirm('申請を取り下げますか？')) return;

    try {
      if (type === 'leave') {
        await requestService.deleteLeaveRequest(id);
      } else {
        await requestService.deleteTimeModificationRequest(id);
      }
      loadRequests();
    } catch (error) {
      console.error('Failed to delete request:', error);
    }
  };

  const getLeaveTypeLabel = (type: string) => {
    switch (type) {
      case 'paid_leave': return '有給休暇';
      case 'sick_leave': return '病気休暇';
      case 'personal_leave': return '私用休暇';
      default: return type;
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'pending':
        return <Badge variant="warning">承認待ち</Badge>;
      case 'approved':
        return <Badge variant="success">承認済み</Badge>;
      case 'rejected':
        return <Badge variant="danger">却下</Badge>;
      default:
        return <Badge variant="default">{status}</Badge>;
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  return (
    <div className="p-6 max-w-6xl mx-auto space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900 flex items-center">
          <DocumentTextIcon className="h-6 w-6 mr-2" />
          申請管理
        </h1>
      </div>

      {/* Tabs */}
      <div className="border-b border-gray-200">
        <nav className="-mb-px flex space-x-8">
          <button
            onClick={() => setActiveTab('leave')}
            className={`py-2 px-1 border-b-2 font-medium text-sm ${
              activeTab === 'leave'
                ? 'border-primary-500 text-primary-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            休暇申請
          </button>
          <button
            onClick={() => setActiveTab('time')}
            className={`py-2 px-1 border-b-2 font-medium text-sm ${
              activeTab === 'time'
                ? 'border-primary-500 text-primary-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            打刻修正申請
          </button>
        </nav>
      </div>

      {/* Leave Requests Tab */}
      {activeTab === 'leave' && (
        <div className="space-y-6">
          <div className="flex justify-end">
            <Button onClick={() => setShowLeaveModal(true)}>
              <PlusIcon className="h-4 w-4 mr-2" />
              新規休暇申請
            </Button>
          </div>

          <div className="grid gap-4">
            {leaveRequests.length === 0 ? (
              <Card>
                <CardContent className="text-center py-8 text-gray-500">
                  休暇申請はありません
                </CardContent>
              </Card>
            ) : (
              leaveRequests.map((request) => (
                <Card key={request.id}>
                  <CardContent className="p-6">
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <div className="flex items-center gap-3 mb-2">
                          <h3 className="font-medium text-gray-900">
                            {getLeaveTypeLabel(request.type)}
                          </h3>
                          {getStatusBadge(request.status)}
                        </div>
                        
                        <div className="text-sm text-gray-600 space-y-1">
                          <p>期間: {format(parseISO(request.startDate), 'yyyy/MM/dd', { locale: ja })} - {format(parseISO(request.endDate), 'yyyy/MM/dd', { locale: ja })}</p>
                          <p>理由: {request.reason}</p>
                          <p>申請日: {format(parseISO(request.createdAt), 'yyyy/MM/dd HH:mm', { locale: ja })}</p>
                          {request.approvedAt && (
                            <p>承認日: {format(parseISO(request.approvedAt), 'yyyy/MM/dd HH:mm', { locale: ja })}</p>
                          )}
                        </div>
                      </div>
                      
                      <div className="flex items-center space-x-2">
                        {request.status === 'pending' && (
                          <Button
                            variant="danger"
                            size="sm"
                            onClick={() => handleDeleteRequest('leave', request.id)}
                          >
                            <TrashIcon className="h-4 w-4" />
                          </Button>
                        )}
                      </div>
                    </div>
                  </CardContent>
                </Card>
              ))
            )}
          </div>
        </div>
      )}

      {/* Time Modification Requests Tab */}
      {activeTab === 'time' && (
        <div className="space-y-6">
          <div className="flex justify-end">
            <Button onClick={() => setShowTimeModal(true)}>
              <PlusIcon className="h-4 w-4 mr-2" />
              新規打刻修正申請
            </Button>
          </div>

          <div className="grid gap-4">
            {timeRequests.length === 0 ? (
              <Card>
                <CardContent className="text-center py-8 text-gray-500">
                  打刻修正申請はありません
                </CardContent>
              </Card>
            ) : (
              timeRequests.map((request) => (
                <Card key={request.id}>
                  <CardContent className="p-6">
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <div className="flex items-center gap-3 mb-2">
                          <h3 className="font-medium text-gray-900">
                            打刻修正申請
                          </h3>
                          {getStatusBadge(request.status)}
                        </div>
                        
                        <div className="text-sm text-gray-600 space-y-1">
                          <p>対象日: {format(parseISO(request.date), 'yyyy/MM/dd（E）', { locale: ja })}</p>
                          {request.requestedClockIn && (
                            <p>出勤時刻: {request.requestedClockIn}</p>
                          )}
                          {request.requestedClockOut && (
                            <p>退勤時刻: {request.requestedClockOut}</p>
                          )}
                          <p>理由: {request.reason}</p>
                          <p>申請日: {format(parseISO(request.createdAt), 'yyyy/MM/dd HH:mm', { locale: ja })}</p>
                        </div>
                      </div>
                      
                      <div className="flex items-center space-x-2">
                        {request.status === 'pending' && (
                          <Button
                            variant="danger"
                            size="sm"
                            onClick={() => handleDeleteRequest('time', request.id)}
                          >
                            <TrashIcon className="h-4 w-4" />
                          </Button>
                        )}
                      </div>
                    </div>
                  </CardContent>
                </Card>
              ))
            )}
          </div>
        </div>
      )}

      {/* Leave Request Modal */}
      <Modal
        isOpen={showLeaveModal}
        onClose={() => setShowLeaveModal(false)}
        title="新規休暇申請"
        size="md"
      >
        <form onSubmit={leaveForm.handleSubmit(onSubmitLeaveRequest)} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              休暇種別 <span className="text-red-500">*</span>
            </label>
            <select
              {...leaveForm.register('type')}
              className="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
            >
              <option value="">選択してください</option>
              <option value="paid_leave">有給休暇</option>
              <option value="sick_leave">病気休暇</option>
              <option value="personal_leave">私用休暇</option>
            </select>
            {leaveForm.formState.errors.type && (
              <p className="mt-1 text-sm text-red-600">{leaveForm.formState.errors.type.message}</p>
            )}
          </div>

          <div className="grid grid-cols-2 gap-4">
            <Input
              label="開始日"
              type="date"
              {...leaveForm.register('startDate')}
              error={leaveForm.formState.errors.startDate?.message}
              required
            />
            <Input
              label="終了日"
              type="date"
              {...leaveForm.register('endDate')}
              error={leaveForm.formState.errors.endDate?.message}
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              理由 <span className="text-red-500">*</span>
            </label>
            <textarea
              {...leaveForm.register('reason')}
              rows={3}
              className="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
              placeholder="休暇の理由を入力してください"
            />
            {leaveForm.formState.errors.reason && (
              <p className="mt-1 text-sm text-red-600">{leaveForm.formState.errors.reason.message}</p>
            )}
          </div>

          <div className="flex justify-end space-x-3 pt-4">
            <Button
              type="button"
              variant="secondary"
              onClick={() => setShowLeaveModal(false)}
            >
              キャンセル
            </Button>
            <Button
              type="submit"
              loading={submitting}
              disabled={submitting}
            >
              申請する
            </Button>
          </div>
        </form>
      </Modal>

      {/* Time Modification Modal */}
      <Modal
        isOpen={showTimeModal}
        onClose={() => setShowTimeModal(false)}
        title="新規打刻修正申請"
        size="md"
      >
        <form onSubmit={timeForm.handleSubmit(onSubmitTimeModification)} className="space-y-4">
          <Input
            label="対象日"
            type="date"
            {...timeForm.register('date')}
            error={timeForm.formState.errors.date?.message}
            required
          />

          <div className="grid grid-cols-2 gap-4">
            <Input
              label="出勤時刻"
              type="time"
              {...timeForm.register('requestedClockIn')}
              helper="修正が必要な場合のみ入力"
            />
            <Input
              label="退勤時刻"
              type="time"
              {...timeForm.register('requestedClockOut')}
              helper="修正が必要な場合のみ入力"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              理由 <span className="text-red-500">*</span>
            </label>
            <textarea
              {...timeForm.register('reason')}
              rows={3}
              className="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
              placeholder="修正が必要な理由を入力してください"
            />
            {timeForm.formState.errors.reason && (
              <p className="mt-1 text-sm text-red-600">{timeForm.formState.errors.reason.message}</p>
            )}
          </div>

          <div className="flex justify-end space-x-3 pt-4">
            <Button
              type="button"
              variant="secondary"
              onClick={() => setShowTimeModal(false)}
            >
              キャンセル
            </Button>
            <Button
              type="submit"
              loading={submitting}
              disabled={submitting}
            >
              申請する
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default RequestsPage;