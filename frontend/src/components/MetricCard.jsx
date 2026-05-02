import React from 'react';

const MetricCard = ({ icon: Icon, label, value, colorClass = "text-emerald-400" }) => {
    return (
        <div className="glass-card relative p-6 rounded-2xl group transition-all duration-300 hover:-translate-y-1 hover:bg-white/[0.05]">
            {/* Ambient Background Glow */}
            <div className={`absolute -top-12 -right-12 w-24 h-24 blur-3xl rounded-full opacity-20 transition-opacity group-hover:opacity-40 ${colorClass.replace('text-', 'bg-')}`} />
            
            <div className="flex items-center space-x-5 relative z-10">
                <div className={`p-4 rounded-xl bg-white/[0.03] border border-white/[0.05] ${colorClass} group-hover:scale-110 transition-transform duration-500`}>
                    <Icon size={28} strokeWidth={1.5} />
                </div>
                <div>
                    <p className="text-slate-500 text-[11px] font-bold tracking-[0.2em] uppercase mb-0.5">{label}</p>
                    <p className="text-3xl font-bold text-white tracking-tight tabular-nums">
                        {value}
                    </p>
                </div>
            </div>
            
            {/* Bottom Accent Line */}
            <div className={`absolute bottom-0 left-6 right-6 h-[2px] rounded-full opacity-0 group-hover:opacity-100 transition-opacity duration-500 bg-gradient-to-r from-transparent via-current to-transparent ${colorClass}`} />
        </div>
    );
};

export default MetricCard;
