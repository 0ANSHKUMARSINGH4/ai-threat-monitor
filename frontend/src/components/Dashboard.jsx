import React, { useEffect, useState } from 'react';
import api, { fetchClients, fetchTrafficStats, resetDemo } from '../services/api';
import ClientTable from './ClientTable';
import TrafficChart from './TrafficChart';
import MetricCard from './MetricCard';
import ThreatCard from './ThreatCard';
import { 
    Activity, ShieldAlert, Cpu, RefreshCcw, 
    LayoutDashboard, Globe, Settings, LogOut, 
    Loader2, ShieldCheck, Waves, Radar,
    Terminal
} from 'lucide-react';

const Dashboard = () => {
    const [clients, setClients] = useState([]);
    const [stats, setStats] = useState({ totalClients: 0, totalRequests: 0, abuseClients: 0 });
    const [error, setError] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const [activeTab, setActiveTab] = useState('dashboard');
    const [lastSync, setLastSync] = useState(new Date());

    const loadData = async () => {
        try {
            const clientData = await fetchClients();
            setClients(Array.isArray(clientData) ? clientData : []);
            
            const statsData = await fetchTrafficStats();
            if (statsData && typeof statsData === 'object' && 'totalClients' in statsData) {
                setStats(statsData);
                setError(null);
            }
            setLastSync(new Date());
            setIsLoading(false);
        } catch (err) {
            console.error('Failed to load data', err);
            setError("Bridge connection unstable. Retrying handshake...");
            setIsLoading(false);
        }
    };

    const handleReset = async () => {
        setIsLoading(true);
        try {
            await resetDemo();
            await loadData();
        } catch (err) {
            alert('Core reset failed: ' + err.message);
        }
    };

    const handleLogout = async () => {
        try {
            await api.post('/auth/logout');
        } catch (e) {
            // Ignore errors — log out regardless
        }
        sessionStorage.removeItem('auth_token');
        window.dispatchEvent(new Event('auth-failed'));
    };

    useEffect(() => {
        loadData();
        const interval = setInterval(loadData, 3000);
        return () => clearInterval(interval);
    }, []);

    const topThreat = (clients || [])
        .filter(c => c.status === 'ABUSIVE' || c.status === 'SUSPICIOUS')
        .sort((a, b) => (b.riskScore || 0) - (a.riskScore || 0))[0];

    return (
        <div className="flex h-screen bg-[#020617] text-slate-200 overflow-hidden font-sans selection:bg-emerald-500/30">
            
            {/* Nav Sidebar */}
            <aside className="hidden md:flex flex-col w-72 h-full bg-[#020617] border-r border-white/5 relative z-30">
                <div className="p-8 pb-10 flex items-center space-x-3">
                    <div className="relative">
                        <Radar size={32} className="text-emerald-500 animate-pulse" />
                        <div className="absolute inset-0 bg-emerald-500/20 blur-xl rounded-full" />
                    </div>
                    <div>
                        <h1 className="text-xl font-black tracking-tighter text-white uppercase italic text-nowrap">AI Threat Monitor</h1>
                        <p className="text-[10px] font-bold text-slate-500 tracking-[0.2em] uppercase">Security Core v2.0</p>
                    </div>
                </div>

                <nav className="flex-1 px-4 space-y-1">
                    <NavBtn 
                        active={activeTab === 'dashboard'} 
                        icon={LayoutDashboard} 
                        label="Command Center" 
                        onClick={() => setActiveTab('dashboard')} 
                    />
                    <NavBtn 
                        active={activeTab === 'logs'} 
                        icon={Waves} 
                        label="Traffic Flow" 
                        onClick={() => setActiveTab('logs')} 
                    />
                    <NavBtn 
                        active={activeTab === 'settings'} 
                        icon={Settings} 
                        label="Defense Policies" 
                        onClick={() => setActiveTab('settings')} 
                    />
                </nav>

                <div className="p-6">
                    <div className="p-4 rounded-2xl bg-white/[0.03] border border-white/5 mb-6">
                        <div className="flex items-center justify-between mb-2">
                            <span className="text-[10px] font-bold text-slate-500 uppercase tracking-wider">Node Status</span>
                            <div className="flex items-center space-x-1">
                                <div className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
                                <span className="text-[10px] font-bold text-emerald-500 uppercase">Live</span>
                            </div>
                        </div>
                        <div className="h-1 bg-white/5 rounded-full overflow-hidden">
                            <div className="h-full bg-emerald-500 w-3/4 animate-[shimmer_2s_infinite]" />
                        </div>
                    </div>
                    
                    <button onClick={handleLogout} className="flex items-center space-x-3 w-full text-slate-500 hover:text-red-400 px-4 py-3 rounded-xl transition-all duration-300 font-bold text-xs uppercase tracking-widest">
                        <LogOut size={16} />
                        <span>Terminate Session</span>
                    </button>
                </div>
            </aside>

            {/* Main Content Area */}
            <main className="flex-1 overflow-y-auto w-full scrollbar-refined relative z-10">
                
                {/* Header Layer */}
                <header className="glass-header px-10 py-6 flex justify-between items-center">
                    <div>
                        <div className="flex items-center space-x-2 mb-1">
                            <div className="w-2 h-2 rounded-full bg-blue-500 animate-pulse" />
                            <span className="text-[10px] font-black text-blue-500 uppercase tracking-[0.3em]">Real-time Interception</span>
                        </div>
                        <h2 className="text-3xl font-black text-white tracking-tighter uppercase italic">Threat Monitor</h2>
                    </div>
                    <div className="flex items-center space-x-6">
                        <div className="text-right hidden sm:block">
                            <p className="text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-0.5">Last Sync</p>
                            <p className="text-xs font-mono text-slate-300">{lastSync.toLocaleTimeString()}</p>
                        </div>
                        <button 
                            onClick={handleReset}
                            className="group flex items-center space-x-3 bg-white text-black hover:bg-emerald-400 transition-all duration-300 px-6 py-3 rounded-full font-black text-xs uppercase tracking-widest"
                        >
                            <RefreshCcw size={14} className={isLoading ? 'animate-spin' : 'group-hover:rotate-180 transition-transform duration-500'} />
                            <span>Recalibrate Core</span>
                        </button>
                    </div>
                </header>

                <div className="p-10 space-y-10 max-w-7xl mx-auto">
                    
                    {error && (
                        <div className="glass-card border-amber-500/20 bg-amber-500/5 p-4 rounded-2xl flex flex-col sm:flex-row items-center justify-between animate-pulse">
                            <div className="flex items-center space-x-3 text-amber-500 mb-4 sm:mb-0">
                                <Terminal size={18} />
                                <span className="text-xs font-bold tracking-widest uppercase">{error}</span>
                            </div>
                            <button 
                                onClick={loadData}
                                className="px-3 py-1 text-xs font-black tracking-widest border border-amber-500 text-amber-400 hover:bg-amber-500 hover:text-black transition-colors duration-200 rounded-md uppercase">
                                RETRY CONNECTION
                            </button>
                        </div>
                    )}

                    {activeTab === 'dashboard' ? (
                        <>
                            {/* Metrics Section */}
                            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                                <MetricCard 
                                    icon={Activity} 
                                    label="Throughput" 
                                    value={stats.totalRequests ? stats.totalRequests.toLocaleString() : "0"} 
                                    colorClass="text-blue-500" 
                                />
                                <MetricCard 
                                    icon={Cpu} 
                                    label="Entities" 
                                    value={stats.totalClients || "0"} 
                                    colorClass="text-purple-500" 
                                />
                                <MetricCard 
                                    icon={ShieldAlert} 
                                    label="Incidents" 
                                    value={stats.abuseClients || "0"} 
                                    colorClass="text-red-500" 
                                />
                                <ThreatCard topThreat={topThreat} />
                            </div>

                            {/* Center Panel (Charts & Stream) */}
                            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                                <div className="lg:col-span-2 glass-card rounded-3xl p-8 relative overflow-hidden">
                                    <div className="flex justify-between items-center mb-8">
                                        <h3 className="text-xs font-black uppercase tracking-[0.3em] text-slate-500">Global Traffic Velocity</h3>
                                        <div className="flex space-x-1">
                                            {[1,2,3].map(i => <div key={i} className="w-1 h-1 rounded-full bg-blue-500/40" />)}
                                        </div>
                                    </div>
                                    <TrafficChart data={clients} />
                                </div>

                                <div className="glass-card rounded-3xl p-8 flex flex-col min-h-[400px]">
                                     <h3 className="text-xs font-black uppercase tracking-[0.3em] text-slate-500 mb-6">Anomaly Vector</h3>
                                     <div className="flex-1 overflow-y-auto scrollbar-refined space-y-4 pr-2">
                                         {(!clients || clients.filter(c => c.status !== 'LEGITIMATE').length === 0) ? (
                                            <div className="flex flex-col items-center justify-center h-full text-slate-600 opacity-40 grayscale">
                                                <ShieldCheck size={48} strokeWidth={1} className="mb-4" />
                                                <p className="text-[10px] font-bold uppercase tracking-widest">Environment Neutral</p>
                                            </div>
                                         ) : (
                                             clients.filter(c => c.status !== 'LEGITIMATE').slice(0, 5).map(c => (
                                                 <div key={c.ipAddress} className={`group p-5 rounded-2xl text-sm border transition-all duration-300 ${c.status === 'ABUSIVE' ? 'bg-red-500/5 border-red-500/20 hover:bg-red-500/10' : 'bg-amber-500/5 border-amber-500/20 hover:bg-amber-500/10'}`}>
                                                     <div className="flex justify-between items-center mb-3">
                                                         <p className="font-mono font-bold text-white text-xs">{c.ipAddress}</p>
                                                         <p className={`text-[10px] font-black px-2 py-1 rounded-md uppercase tracking-tighter ${c.status === 'ABUSIVE' ? 'bg-red-500 text-white' : 'bg-amber-500 text-black'}`}>{c.status}</p>
                                                     </div>
                                                     <p className="text-slate-500 text-[11px] leading-relaxed line-clamp-2 italic">"{c.aiReason || 'Analyzing behavioral footprint...'}"</p>
                                                 </div>
                                             ))
                                         )}
                                     </div>
                                </div>
                            </div>

                            {/* Client Repository Table */}
                            <div className="glass-card rounded-3xl overflow-hidden">
                                <div className="p-8 border-b border-white/5 flex justify-between items-center">
                                    <h3 className="text-xs font-black uppercase tracking-[0.3em] text-slate-500">Live Client Data-Bank</h3>
                                    {isLoading && <Loader2 size={16} className="text-emerald-500 animate-spin" />}
                                </div>
                                <div className="overflow-x-auto">
                                    {!clients || clients.length === 0 ? (
                                        <div className="p-24 text-center text-slate-600 flex flex-col items-center">
                                            <Activity className="opacity-10 mb-6" size={64} />
                                            <p className="text-xs font-bold uppercase tracking-[0.4em]">Awaiting Uplink...</p>
                                        </div>
                                    ) : (
                                        <ClientTable clients={clients} refreshData={loadData} />
                                    )}
                                </div>
                            </div>
                        </>
                    ) : (
                        <div className="flex flex-col items-center justify-center py-40 glass-card rounded-3xl text-slate-600">
                             <Terminal size={64} strokeWidth={1} className="mb-8 opacity-20" />
                             <h3 className="text-xl font-black text-white uppercase italic tracking-tighter">Encrypted Protocol</h3>
                             <p className="mt-4 text-xs font-bold uppercase tracking-[0.2em] text-center max-w-sm leading-relaxed">System integration for this module is restricted. Contact network architect for decryption keys.</p>
                        </div>
                    )}
                </div>
            </main>
        </div>
    );
};

const NavBtn = ({ active, icon: Icon, label, onClick }) => (
    <button 
        onClick={onClick} 
        className={`flex items-center space-x-4 w-full px-6 py-4 rounded-2xl text-[11px] font-black uppercase tracking-widest transition-all duration-500 ${active ? 'bg-white text-black scale-[1.02] shadow-[0_10px_20px_-5px_rgba(255,255,255,0.2)]' : 'text-slate-500 hover:text-white hover:bg-white/5'}`}
    >
        <Icon size={18} strokeWidth={2} />
        <span>{label}</span>
    </button>
);

export default Dashboard;
