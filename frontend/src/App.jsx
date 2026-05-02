import React, { useState, useEffect } from 'react'
import Dashboard from './components/Dashboard'
import { Lock } from 'lucide-react'

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(!!localStorage.getItem('adminCredentials'));
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');

  useEffect(() => {
    const handleAuthFailed = () => setIsAuthenticated(false);
    window.addEventListener('auth-failed', handleAuthFailed);
    return () => window.removeEventListener('auth-failed', handleAuthFailed);
  }, []);

  const handleLogin = (e) => {
    e.preventDefault();
    if (username && password) {
      const credentials = btoa(`${username}:${password}`);
      localStorage.setItem('adminCredentials', credentials);
      setIsAuthenticated(true);
    }
  };

  if (!isAuthenticated) {
    return (
      <div className="min-h-screen bg-slate-950 flex items-center justify-center p-4">
        <div className="max-w-md w-full bg-slate-900 border border-slate-800 rounded-xl shadow-2xl p-8">
          <div className="flex flex-col items-center mb-8">
            <div className="p-3 bg-blue-500/10 rounded-full mb-4">
              <Lock className="w-8 h-8 text-blue-400" />
            </div>
            <h1 className="text-2xl font-bold text-white tracking-tight">Admin Access</h1>
            <p className="text-slate-400 text-sm mt-2 text-center">Enter your credentials to manage the AI Threat Monitor dashboard.</p>
          </div>
          
          <form onSubmit={handleLogin} className="space-y-6">
            <div>
              <label className="block text-sm font-medium text-slate-300 mb-2">Username</label>
              <input
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                className="w-full bg-slate-950 border border-slate-800 rounded-lg px-4 py-3 text-white placeholder-slate-600 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500 transition-colors"
                placeholder="admin"
                required
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-300 mb-2">Password</label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full bg-slate-950 border border-slate-800 rounded-lg px-4 py-3 text-white placeholder-slate-600 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500 transition-colors"
                placeholder="••••••••"
                required
              />
            </div>
            <button
              type="submit"
              className="w-full bg-blue-600 hover:bg-blue-500 text-white font-semibold py-3 px-4 rounded-lg transition-colors duration-200"
            >
              Sign In
            </button>
          </form>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen">
      <Dashboard />
    </div>
  );
}

export default App
