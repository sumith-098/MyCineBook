import { createContext, useContext, useState, useCallback } from 'react'
import { setTokens, clearTokens, getRole } from '../api/client'
import { authApi } from '../api/auth'

const AuthContext = createContext(null)

function loadUser() {
  const raw = localStorage.getItem('cinebook_user')
  return raw ? JSON.parse(raw) : null
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(loadUser())
  const [role, setRole] = useState(getRole())

  const applyAuthResponse = useCallback((data) => {
    setTokens({ accessToken: data.accessToken, refreshToken: data.refreshToken, role: data.role })
    const nextUser = { userId: data.userId, name: data.name, email: data.email }
    localStorage.setItem('cinebook_user', JSON.stringify(nextUser))
    setUser(nextUser)
    setRole(data.role)
    return nextUser
  }, [])

  const loginCustomer = useCallback(async (email, password) => {
    const data = await authApi.loginCustomer({ email, password })
    return applyAuthResponse(data)
  }, [applyAuthResponse])

  const loginOwner = useCallback(async (email, password) => {
    const data = await authApi.loginOwner({ email, password })
    return applyAuthResponse(data)
  }, [applyAuthResponse])

  const loginAdmin = useCallback(async (email, password) => {
    const data = await authApi.loginAdmin({ email, password })
    return applyAuthResponse(data)
  }, [applyAuthResponse])

  const verifyCustomerOtpAndLogin = useCallback(async (email, otp) => {
    const data = await authApi.verifyCustomerOtp({ email, otp })
    return applyAuthResponse(data)
  }, [applyAuthResponse])

  const logout = useCallback(() => {
    clearTokens()
    localStorage.removeItem('cinebook_user')
    setUser(null)
    setRole(null)
  }, [])

  const value = {
    user,
    role,
    isAuthenticated: !!user,
    loginCustomer,
    loginOwner,
    loginAdmin,
    verifyCustomerOtpAndLogin,
    logout,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
