import api, { call } from './client'

export const paymentApi = {
  createOrder: (payload) => call(api.post('/api/payments/razorpay/create-order', payload)),
  verify: (payload) => call(api.post('/api/payments/razorpay/verify', payload)),
}
