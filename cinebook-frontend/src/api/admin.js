import api, { call } from './client'

export const adminApi = {
  // owned by auth-service, called through the gateway
  pendingOwners: () => call(api.get('/api/auth/admin/owners/pending')),
  approveOwner: (ownerId) => call(api.post(`/api/auth/admin/owners/${ownerId}/approve`)),
  changePassword: (payload) => call(api.put('/api/auth/admin/change-password', payload)),

  // owned by admin-service
  dashboard: () => call(api.get('/api/admin/dashboard')),
  settlements: () => call(api.get('/api/admin/settlements')),
  markSettlementPaid: (ownerId, payload) => call(api.post(`/api/admin/settlements/${ownerId}/pay`, payload)),
  getCommissionConfig: () => call(api.get('/api/admin/config')),
  updateCommissionConfig: (payload) => call(api.put('/api/admin/config', payload)),
}