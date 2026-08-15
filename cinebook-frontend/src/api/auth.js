import api, { call } from './client'

export const authApi = {
  // customer
  registerCustomer: (payload) => call(api.post('/api/auth/customer/register', payload)),
  verifyCustomerOtp: (payload) => call(api.post('/api/auth/customer/verify-otp', payload)),
  resendOtp: (purpose, email) => call(api.post(`/api/auth/customer/resend-otp/${purpose}?email=${encodeURIComponent(email)}`)),
  loginCustomer: (payload) => call(api.post('/api/auth/customer/login', payload)),
  forgotPassword: (email) => call(api.post('/api/auth/customer/forgot-password', { email })),
  verifyResetOtp: (payload) => call(api.post('/api/auth/customer/verify-reset-otp', payload)),
  resetPassword: (payload) => call(api.post('/api/auth/customer/reset-password', payload)),

  // owner
  registerOwner: (payload) => call(api.post('/api/auth/owner/register', payload)),
  verifyOwnerOtp: (payload) => call(api.post('/api/auth/owner/verify-otp', payload)),
  loginOwner: (payload) => call(api.post('/api/auth/owner/login', payload)),

  // admin
  loginAdmin: (payload) => call(api.post('/api/auth/admin/login', payload)),
}
