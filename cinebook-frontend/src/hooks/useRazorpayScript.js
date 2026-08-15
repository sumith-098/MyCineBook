import { useEffect, useState } from 'react'

const SRC = 'https://checkout.razorpay.com/v1/checkout.js'

export function useRazorpayScript() {
  const [ready, setReady] = useState(!!window.Razorpay)

  useEffect(() => {
    if (window.Razorpay) { setReady(true); return }
    const existing = document.querySelector(`script[src="${SRC}"]`)
    if (existing) {
      existing.addEventListener('load', () => setReady(true))
      return
    }
    const script = document.createElement('script')
    script.src = SRC
    script.async = true
    script.onload = () => setReady(true)
    document.body.appendChild(script)
  }, [])

  return ready
}
