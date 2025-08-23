import React, { useState, useEffect } from 'react';
import { format, parseISO } from 'date-fns';
import { ja } from 'date-fns/locale';
import { 
  DocumentTextIcon, 
  CheckIcon, 
  XMarkIcon,
  UserIcon,
  EyeIcon
} from '@heroicons/react/24/outline';

import { 
  Card, 
  CardContent,
  Badge,
  Button,
  Modal,
  LoadingSpinner
} from '../../components/ui';
import { requestService } from '../../services/requestService';
import { managerService } from '../../services/managerService';
import type { LeaveRequest, TimeModificationRequest, User } from '../../types';

const ApprovalQueue: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'leave' | 'time'>('leave');
  const [leaveRequests, setLeaveRequests] = useState<LeaveRequest[]>([]);
  const [timeRequests, setTimeRequests] = useState<TimeModificationRequest[]>([]);
  const [teamMembers, setTeamMembers] = useState<User[]>([]);
  const [selectedRequest, setSelectedRequest] = useState<LeaveRequest | TimeModificationRequest | null>(null);
  const [showDetailModal, setShowDetailModal] = useState(false);
  const [loading, setLoading] = useState(true);
  const [processing, setProcessing] = useState(false);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    try {
      const [leaveResponse, timeResponse, teamResponse] = await Promise.all([
        requestService.getLeaveRequests('PENDING'),
        requestService.getTimeModificationRequests('PENDING'),
        managerService.getTeamMembers(),
      ]);

      if (leaveResponse.success && leaveResponse.data) {
        setLeaveRequests(leaveResponse.data);
      }
      if (timeResponse.success && timeResponse.data) {
        setTimeRequests(timeResponse.data);
      }
      if (teamResponse.success && teamResponse.data) {
        setTeamMembers(teamResponse.data);
      }
    } catch (error) {
      console.error('Failed to load approval data:', error);
    } finally {
      setLoading(false);
    }
  };

  const getMemberName = (userId: string): string => {
    const member = teamMembers.find(m => m.id === userId);
    return member?.name || 'Unknown User';
  };

  const handleApproval = async (
    type: 'leave' | 'time',
    requestId: string,
    status: 'APPROVED' | 'REJECTED'
  ) => {
    const confirmMessage = status === 'APPROVED' 
      ? '申請を承認しますか？' 
      : '申請を却下しますか？';
    
    if (!confirm(confirmMessage)) return;

    setProcessing(true);
    try {
      if (type === 'leave') {
        await requestService.updateLeaveRequestStatus(requestId, status);
      } else {
        await requestService.updateTimeModificationStatus(requestId, status);
      }
      
      setShowDetailModal(false);
      setSelectedRequest(null);
      loadData();
    } catch (error) {
      console.error('Failed to update request status:', error);
    } finally {
      setProcessing(false);
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

  const calculateLeaveDays = (startDate: string, endDate: string): number => {
    const start = new Date(startDate);
    const end = new Date(endDate);
    const diffTime = end.getTime() - start.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;
    return diffDays;
  };

  const openDetailModal = (request: LeaveRequest | TimeModificationRequest) => {
    setSelectedRequest(request);
    setShowDetailModal(true);
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
        <h1 className="text-2xl font-bold text-foreground flex items-center">
          <DocumentTextIcon className="h-6 w-6 mr-2" />
          承認管理
        </h1>
        <div className="text-sm text-muted-foreground">
          承認待ち: {leaveRequests.length + timeRequests.length}件
        </div>
      </div>

      {/* Tabs */}
      <div className="border-b border-gray-200">
        <nav className="-mb-px flex space-x-8">
          <button
            onClick={() => setActiveTab('leave')}
            className={`py-2 px-1 border-b-2 font-medium text-sm ${
              activeTab === 'leave'
                ? 'border-primary-500 text-primary'
                : 'border-transparent text-muted-foreground hover:text-foreground hover:border-gray-300'
            }`}
          >
            休暇申請 ({leaveRequests.length})
          </button>
          <button
            onClick={() => setActiveTab('time')}
            className={`py-2 px-1 border-b-2 font-medium text-sm ${
              activeTab === 'time'
                ? 'border-primary-500 text-primary'
                : 'border-transparent text-muted-foreground hover:text-foreground hover:border-gray-300'
            }`}
          >
            打刻修正申請 ({timeRequests.length})
          </button>
        </nav>
      </div>

      {/* Leave Requests Tab */}
      {activeTab === 'leave' && (
        <div className="space-y-4">
          {leaveRequests.length === 0 ? (
            <Card>
              <CardContent className="text-center py-8 text-muted-foreground">
                承認待ちの休暇申請はありません
              </CardContent>
            </Card>
          ) : (
            leaveRequests.map((request) => (
              <Card key={request.id} className="hover:shadow-md transition-shadow">
                <CardContent className="p-6">
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <div className="flex items-center gap-3 mb-3">
                        <div className="w-10 h-10 bg-muted rounded-full flex items-center justify-center">
                          <UserIcon className="h-6 w-6 text-muted-foreground" />
                        </div>
                        <div>
                          <h3 className="font-medium text-foreground">
                            {getMemberName(request.userId)}
                          </h3>
                          <p className="text-sm text-muted-foreground">
                            {getLeaveTypeLabel(request.type)}
                          </p>
                        </div>
                        <Badge variant="warning" size="sm">承認待ち</Badge>
                      </div>

                      <div className="grid grid-cols-2 gap-4 text-sm mb-4">
                        <div>
                          <p className="text-muted-foreground">申請期間</p>
                          <p className="font-medium">
                            {format(parseISO(request.startDate), 'yyyy/MM/dd', { locale: ja })} - {format(parseISO(request.endDate), 'yyyy/MM/dd', { locale: ja })}
                          </p>
                          <p className="text-xs text-muted-foreground">
                            ({calculateLeaveDays(request.startDate, request.endDate)}日間)
                          </p>
                        </div>
                        <div>
                          <p className="text-muted-foreground">申請日時</p>
                          <p className="font-medium">
                            {format(parseISO(request.createdAt), 'yyyy/MM/dd HH:mm', { locale: ja })}
                          </p>
                        </div>
                      </div>

                      <div className="mb-4">
                        <p className="text-muted-foreground text-sm mb-1">理由</p>
                        <p className="text-foreground text-sm bg-muted/50 p-2 rounded">
                          {request.reason}
                        </p>
                      </div>
                    </div>

                    <div className="flex flex-col space-y-2 ml-4">
                      <Button
                        size="sm"
                        variant="ghost"
                        onClick={() => openDetailModal(request)}
                      >
                        <EyeIcon className="h-4 w-4" />
                      </Button>
                      <Button
                        size="sm"
                        variant="primary"
                        onClick={() => handleApproval('leave', request.id, 'APPROVED')}
                        disabled={processing}
                      >
                        <CheckIcon className="h-4 w-4" />
                      </Button>
                      <Button
                        size="sm"
                        variant="danger"
                        onClick={() => handleApproval('leave', request.id, 'REJECTED')}
                        disabled={processing}
                      >
                        <XMarkIcon className="h-4 w-4" />
                      </Button>
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))
          )}
        </div>
      )}

      {/* Time Modification Requests Tab */}
      {activeTab === 'time' && (
        <div className="space-y-4">
          {timeRequests.length === 0 ? (
            <Card>
              <CardContent className="text-center py-8 text-muted-foreground">
                承認待ちの打刻修正申請はありません
              </CardContent>
            </Card>
          ) : (
            timeRequests.map((request) => (
              <Card key={request.id} className="hover:shadow-md transition-shadow">
                <CardContent className="p-6">
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <div className="flex items-center gap-3 mb-3">
                        <div className="w-10 h-10 bg-muted rounded-full flex items-center justify-center">
                          <UserIcon className="h-6 w-6 text-muted-foreground" />
                        </div>
                        <div>
                          <h3 className="font-medium text-foreground">
                            {getMemberName(request.userId)}
                          </h3>
                          <p className="text-sm text-muted-foreground">打刻修正申請</p>
                        </div>
                        <Badge variant="warning" size="sm">承認待ち</Badge>
                      </div>

                      <div className="grid grid-cols-2 gap-4 text-sm mb-4">
                        <div>
                          <p className="text-muted-foreground">対象日</p>
                          <p className="font-medium">
                            {format(parseISO(request.date), 'yyyy/MM/dd（E）', { locale: ja })}
                          </p>
                        </div>
                        <div>
                          <p className="text-muted-foreground">申請日時</p>
                          <p className="font-medium">
                            {format(parseISO(request.createdAt), 'yyyy/MM/dd HH:mm', { locale: ja })}
                          </p>
                        </div>
                      </div>

                      {(request.requestedClockIn || request.requestedClockOut) && (
                        <div className="grid grid-cols-2 gap-4 text-sm mb-4">
                          {request.requestedClockIn && (
                            <div>
                              <p className="text-muted-foreground">修正後出勤時刻</p>
                              <p className="font-medium text-primary">
                                {request.requestedClockIn}
                              </p>
                            </div>
                          )}
                          {request.requestedClockOut && (
                            <div>
                              <p className="text-muted-foreground">修正後退勤時刻</p>
                              <p className="font-medium text-primary">
                                {request.requestedClockOut}
                              </p>
                            </div>
                          )}
                        </div>
                      )}

                      <div className="mb-4">
                        <p className="text-muted-foreground text-sm mb-1">理由</p>
                        <p className="text-foreground text-sm bg-muted/50 p-2 rounded">
                          {request.reason}
                        </p>
                      </div>
                    </div>

                    <div className="flex flex-col space-y-2 ml-4">
                      <Button
                        size="sm"
                        variant="ghost"
                        onClick={() => openDetailModal(request)}
                      >
                        <EyeIcon className="h-4 w-4" />
                      </Button>
                      <Button
                        size="sm"
                        variant="primary"
                        onClick={() => handleApproval('time', request.id, 'APPROVED')}
                        disabled={processing}
                      >
                        <CheckIcon className="h-4 w-4" />
                      </Button>
                      <Button
                        size="sm"
                        variant="danger"
                        onClick={() => handleApproval('time', request.id, 'REJECTED')}
                        disabled={processing}
                      >
                        <XMarkIcon className="h-4 w-4" />
                      </Button>
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))
          )}
        </div>
      )}

      {/* Detail Modal */}
      <Modal
        isOpen={showDetailModal}
        onClose={() => setShowDetailModal(false)}
        title="申請詳細"
        size="md"
      >
        {selectedRequest && (
          <div className="space-y-4">
            <div className="flex items-center gap-3">
              <div className="w-12 h-12 bg-muted rounded-full flex items-center justify-center">
                <UserIcon className="h-8 w-8 text-muted-foreground" />
              </div>
              <div>
                <h3 className="font-medium text-foreground text-lg">
                  {getMemberName(selectedRequest.userId)}
                </h3>
                <p className="text-sm text-muted-foreground">
                  {'type' in selectedRequest 
                    ? getLeaveTypeLabel(selectedRequest.type)
                    : '打刻修正申請'
                  }
                </p>
              </div>
            </div>

            <div className="border-t pt-4">
              {'type' in selectedRequest ? (
                // Leave Request Details
                <div className="space-y-3">
                  <div>
                    <label className="text-sm font-medium text-muted-foreground">申請期間</label>
                    <p className="text-foreground">
                      {format(parseISO(selectedRequest.startDate), 'yyyy年M月d日', { locale: ja })} - {format(parseISO(selectedRequest.endDate), 'yyyy年M月d日', { locale: ja })}
                    </p>
                    <p className="text-sm text-muted-foreground">
                      ({calculateLeaveDays(selectedRequest.startDate, selectedRequest.endDate)}日間)
                    </p>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-muted-foreground">理由</label>
                    <p className="text-foreground mt-1">{selectedRequest.reason}</p>
                  </div>
                </div>
              ) : (
                // Time Modification Request Details
                <div className="space-y-3">
                  <div>
                    <label className="text-sm font-medium text-muted-foreground">対象日</label>
                    <p className="text-foreground">
                      {format(parseISO(selectedRequest.date), 'yyyy年M月d日（E）', { locale: ja })}
                    </p>
                  </div>
                  {selectedRequest.requestedClockIn && (
                    <div>
                      <label className="text-sm font-medium text-muted-foreground">修正後出勤時刻</label>
                      <p className="text-foreground">{selectedRequest.requestedClockIn}</p>
                    </div>
                  )}
                  {selectedRequest.requestedClockOut && (
                    <div>
                      <label className="text-sm font-medium text-muted-foreground">修正後退勤時刻</label>
                      <p className="text-foreground">{selectedRequest.requestedClockOut}</p>
                    </div>
                  )}
                  <div>
                    <label className="text-sm font-medium text-muted-foreground">理由</label>
                    <p className="text-foreground mt-1">{selectedRequest.reason}</p>
                  </div>
                </div>
              )}

              <div className="mt-4 pt-4 border-t">
                <label className="text-sm font-medium text-muted-foreground">申請日時</label>
                <p className="text-foreground">
                  {format(parseISO(selectedRequest.createdAt), 'yyyy年M月d日 HH:mm', { locale: ja })}
                </p>
              </div>
            </div>

            <div className="flex justify-end space-x-3 pt-4 border-t">
              <Button
                variant="danger"
                onClick={() => handleApproval(
                  'type' in selectedRequest ? 'leave' : 'time',
                  selectedRequest.id,
                  'REJECTED'
                )}
                loading={processing}
                disabled={processing}
              >
                <XMarkIcon className="h-4 w-4 mr-2" />
                却下
              </Button>
              <Button
                variant="primary"
                onClick={() => handleApproval(
                  'type' in selectedRequest ? 'leave' : 'time',
                  selectedRequest.id,
                  'APPROVED'
                )}
                loading={processing}
                disabled={processing}
              >
                <CheckIcon className="h-4 w-4 mr-2" />
                承認
              </Button>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
};

export default ApprovalQueue;