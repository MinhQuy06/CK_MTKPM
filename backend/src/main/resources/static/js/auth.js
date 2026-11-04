// ============================================================
// auth.js — Auth session manager
// ============================================================
const Auth = {
  basePath() {
    return window.location.pathname.startsWith('/frontend/') ? '/frontend' : '';
  },
  getUser() {
    const u = localStorage.getItem('user');
    return u ? JSON.parse(u) : null;
  },

  setSession(authData) {
    localStorage.setItem('accessToken', authData.accessToken);
    localStorage.setItem('refreshToken', authData.refreshToken);
    localStorage.setItem('user', JSON.stringify({
      id: authData.userId,
      email: authData.email,
      role: authData.role,
      fullName: authData.fullName
    }));
  },

  logout() {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    window.location.href = `${this.basePath()}/login.html`;
  },

  checkAuth(allowedRoles = []) {
    const user = this.getUser();
    if (!user) {
      window.location.href = `${this.basePath()}/login.html`;
      return null;
    }
    if (allowedRoles.length > 0 && !allowedRoles.includes(user.role)) {
      alert('Bạn không có quyền truy cập trang này!');
      this.redirectByRole(user.role);
      return null;
    }
    return user;
  },

  redirectByRole(role) {
    switch (role) {
      case 'ADMIN': window.location.href = `${this.basePath()}/admin/dashboard.html`; break;
      case 'DOCTOR': window.location.href = `${this.basePath()}/doctor/dashboard.html`; break;
      case 'PATIENT': window.location.href = `${this.basePath()}/patient/dashboard.html`; break;
      default: window.location.href = `${this.basePath()}/index.html`;
    }
  }
};
