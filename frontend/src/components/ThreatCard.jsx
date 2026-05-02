import React from 'react';
import { AlertOctagon, ShieldCheck, Zap } from 'lucide-react';

const ThreatCard = ({ topThreat }) => {
    if (!topThreat) {
        return (
            <div className="glass-card relative p-6 rounded-2xl h-full flex items-center space-x-5 group hover:bg-white/[0.05] transition-all duration-300">
               <div className="p-4 rounded-xl bg-emerald-500/10 text-emerald-500 border border-emerald-500/20 group-hover:scale-110 transition-transform duration-500">
                   <ShieldCheck size={28} strokeWidth={1.5} />
               </div>
               <div>
                   <p className="text-emerald-400 font-bold uppercase tracking-[0.2em] text-[10px] mb-1">Status: Secure</p>
                   <p className="text-slate-400 text-sm font-medium">No behavioral anomalies identified.</p>
               </div>
            </div>
        );
    }

    const isHighRisk = topThreat.riskScore >= 70;
    const accentColor = isHighRisk ? 'text-red-500' : 'text-amber-500';
    const accentBg = isHighRisk ? 'bg-red-500/10' : 'bg-amber-500/10';
    const accentBorder = isHighRisk ? 'border-red-500/20' : 'border-amber-500/20';

    return (
        <div className={`glass-card relative p-6 rounded-2xl h-full overflow-hidden flex items-center space-x-5 group transition-all duration-300 hover:bg-white/[0.05] ${isHighRisk ? 'neon-shadow-red' : 'neon-shadow-amber'}`}>
            {/* Dynamic Hazard Background */}
            <div className={`absolute -top-10 -right-10 w-40 h-40 blur-[80px] rounded-full opacity-20 ${isHighRisk ? 'bg-red-500' : 'bg-amber-500'}`} />
            
            <div className={`p-4 rounded-xl ${accentBg} ${accentColor} border ${accentBorder} animate-pulse-slow relative z-10`}>
                <AlertOctagon size={28} strokeWidth={1.5} />
            </div>

            <div className="z-10 flex-1 min-w-0">
                <div className="flex items-center space-x-2 mb-1">
                    <Zap size={10} className={`${accentColor} fill-current`} />
                    <p className={`${accentColor} font-bold uppercase tracking-[0.2em] text-[10px]`}>Incident Detected</p>
                </div>
                <div className="flex justify-between items-center space-x-4">
                    <p className="text-xl font-mono font-bold text-white tracking-tight truncate">
                        {topThreat.ipAddress}
                    </p>
                    <div className="text-right">
                        <span className={`text-2xl font-bold tabular-nums ${accentColor}`}>{topThreat.riskScore}</span>
                        <span className="text-[10px] text-slate-500 font-bold ml-1 uppercase">Risk</span>
                    </div>
                </div>
            </div>

            {/* Warning Stripe */}
            <div className={`absolute left-0 top-0 bottom-0 w-[3px] ${isHighRisk ? 'bg-red-500' : 'bg-amber-500'}`} />
        </div>
    );
};

export default ThreatCard;
