import React from 'react';
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

const TrafficChart = ({ data }) => {
    // Top 10 clients by requests per minute
    const chartData = [...(data || [])]
        .sort((a, b) => b.requestsPerMinute - a.requestsPerMinute)
        .slice(0, 10)
        .map(client => ({
            name: client.ipAddress,
            rpm: parseFloat(client.requestsPerMinute?.toFixed(1) || 0),
        }));

    return (
        <div className="h-72 w-full mt-4">
            <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={chartData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                    <defs>
                        <linearGradient id="colorRpm" x1="0" y1="0" x2="0" y2="1">
                            <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.3}/>
                            <stop offset="95%" stopColor="#3b82f6" stopOpacity={0}/>
                        </linearGradient>
                    </defs>
                    <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" vertical={false} />
                    <XAxis 
                        dataKey="name" 
                        stroke="#64748b" 
                        fontSize={10} 
                        tickLine={false} 
                        axisLine={false} 
                        dy={10}
                        fontFamily="monospace"
                    />
                    <YAxis 
                        stroke="#64748b" 
                        fontSize={10} 
                        tickLine={false} 
                        axisLine={false} 
                        dx={-10}
                    />
                    <Tooltip 
                        contentStyle={{ 
                            backgroundColor: 'rgba(15, 23, 42, 0.9)', 
                            border: '1px solid rgba(255, 255, 255, 0.1)', 
                            borderRadius: '12px', 
                            color: '#f8fafc', 
                            boxShadow: '0 10px 15px -3px rgba(0, 0, 0, 0.5)',
                            backdropFilter: 'blur(8px)'
                        }}
                        itemStyle={{ color: '#60a5fa', fontWeight: 'bold', fontSize: '12px' }}
                        labelStyle={{ fontSize: '11px', color: '#94a3b8', marginBottom: '4px', fontFamily: 'monospace' }}
                        cursor={{stroke: 'rgba(255,255,255,0.1)', strokeWidth: 1}}
                    />
                    <Area 
                        type="monotone" 
                        dataKey="rpm" 
                        stroke="#3b82f6" 
                        strokeWidth={2}
                        fillOpacity={1} 
                        fill="url(#colorRpm)" 
                        animationDuration={1500}
                        activeDot={{ r: 4, strokeWidth: 0, fill: '#60a5fa' }}
                    />
                </AreaChart>
            </ResponsiveContainer>
        </div>
    );
};

export default TrafficChart;
