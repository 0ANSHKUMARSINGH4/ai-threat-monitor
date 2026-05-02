import React from 'react';
import { ShieldCheck, ShieldAlert, Zap, ExternalLink } from 'lucide-react';
import { unblockClient } from '../services/api';

const ClientTable = ({ clients, refreshData }) => {
    
    const handleUnblock = async (ip) => {
        try {
            await unblockClient(ip);
            refreshData();
        } catch (err) {
            alert('Failed to unblock: ' + err.message);
        }
    };

    return (
        <table className="w-full text-left border-collapse min-w-[800px]">
            <thead>
                <tr className="border-b border-white/5 bg-white/[0.01]">
                    <th className="px-8 py-5 text-[10px] font-black text-slate-500 uppercase tracking-[0.2em]">End-User Identity</th>
                    <th className="px-8 py-5 text-[10px] font-black text-slate-500 uppercase tracking-[0.2em]">Risk Profile</th>
                    <th className="px-8 py-5 text-[10px] font-black text-slate-500 uppercase tracking-[0.2em]">Traffic Volume</th>
                    <th className="px-8 py-5 text-[10px] font-black text-slate-500 uppercase tracking-[0.2em]">Security Analysis</th>
                    <th className="px-8 py-5 text-[10px] font-black text-slate-500 uppercase tracking-[0.2em] text-right">Countermeasures</th>
                </tr>
            </thead>
            <tbody className="divide-y divide-white/[0.03]">
                {clients.map((client) => {
                    const statusColor = 
                        client.status === 'ABUSIVE' ? 'text-red-500 bg-red-500/10' : 
                        client.status === 'SUSPICIOUS' ? 'text-amber-500 bg-amber-500/10' : 
                        'text-emerald-500 bg-emerald-500/10';

                    return (
                        <tr key={client.ipAddress} className="group hover:bg-white/[0.02] transition-all duration-300">
                            <td className="px-8 py-6">
                                <div className="flex items-center space-x-3">
                                    <div className="p-2 rounded-lg bg-slate-800/50 text-slate-400 group-hover:text-blue-400 group-hover:bg-blue-400/10 transition-colors">
                                        <Zap size={14} />
                                    </div>
                                    <span className="font-mono font-bold text-slate-200 text-sm tracking-tight">{client.ipAddress}</span>
                                </div>
                            </td>
                            <td className="px-8 py-6">
                                <span className={`inline-flex items-center px-3 py-1 rounded-full text-[10px] font-black uppercase tracking-tighter ${statusColor}`}>
                                    {client.status === 'LEGITIMATE' ? <ShieldCheck size={10} className="mr-1.5" /> : <ShieldAlert size={10} className="mr-1.5" />}
                                    {client.status}
                                </span>
                            </td>
                            <td className="px-8 py-6">
                                <div className="flex flex-col">
                                    <span className="text-sm font-bold text-white tabular-nums">{client.requestsPerMinute?.toFixed(1) || 0}</span>
                                    <span className="text-[10px] font-bold text-slate-500 uppercase tracking-widest">req / min</span>
                                </div>
                            </td>
                            <td className="px-8 py-6 max-w-xs">
                                <p className="text-xs text-slate-400 italic line-clamp-1 group-hover:line-clamp-none transition-all duration-500">
                                    {client.aiReason || 'Passive monitoring active...'}
                                </p>
                            </td>
                            <td className="px-8 py-6 text-right">
                                {client.status !== 'LEGITIMATE' ? (
                                    <button 
                                        onClick={() => handleUnblock(client.ipAddress)}
                                        className="inline-flex items-center space-x-2 bg-emerald-500 text-black px-4 py-2 rounded-lg font-black text-[10px] uppercase tracking-widest hover:bg-emerald-400 hover:scale-105 active:scale-95 transition-all duration-300 shadow-lg shadow-emerald-500/20"
                                    >
                                        <span>Authorize</span>
                                    </button>
                                ) : (
                                    <span className="text-[10px] font-black text-slate-600 uppercase tracking-[0.2em] flex items-center justify-end">
                                        Active <ExternalLink size={10} className="ml-2 opacity-20" />
                                    </span>
                                )}
                            </td>
                        </tr>
                    );
                })}
            </tbody>
        </table>
    );
};

export default ClientTable;
