import React from 'react';
import { Check, Plus, Share2, Facebook, Twitter, Loader2, Globe, Users, Zap } from 'lucide-react';

const SocialConnections = ({
                               fbConnected,
                               fbStatusLoading,
                               onConnectFacebook,
                               twConnected,
                               twStatusLoading,
                               onConnectTwitter
                           }) => {
    return (
        <div className="bg-gradient-to-r from-slate-800 to-slate-900 p-4 rounded-xl shadow-lg border border-slate-700/50 backdrop-blur-sm">
            <div className="flex items-center justify-between">
                <div className="flex items-center">
                    <div className="p-1.5 bg-blue-500/20 rounded-lg mr-3">
                        <Globe className="w-4 h-4 text-blue-400" />
                    </div>
                    <div>
                        <h3 className="text-sm font-semibold text-white">Social Connections</h3>
                        <p className="text-xs text-slate-400">
                            {[fbConnected, twConnected].filter(Boolean).length}/2 connected
                        </p>
                    </div>
                </div>

                <div className="flex items-center space-x-2">
                    {/* Facebook Connection */}
                    <button
                        className={`py-2 px-4 rounded-lg flex items-center justify-center shadow-md transition-colors duration-300 ${
                            fbConnected ? 'bg-gray-400 text-white cursor-not-allowed' : 'bg-blue-600 text-white hover:bg-blue-700'
                        }`}
                        onClick={fbConnected ? undefined : onConnectFacebook}
                        disabled={fbConnected || fbStatusLoading}
                    >
                        <svg className="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 24 24">
                            <path d="M14 13.5h2.5l1-4H14V6.5c0-.97.312-1.637 1.7-1.637H18V.992C17.477.93 16.088.837 14.704.837 11.98 1.05 10 2.73 10 6.125V9.5H7.5v4H10V23h4v-9.5z" />
                        </svg>
                        {fbStatusLoading ? 'Checking...' : fbConnected ? 'Facebook Connected' : 'Connect Facebook'}
                    </button>
                    <button
                        className={`py-2 px-4 rounded-lg flex items-center justify-center shadow-md transition-colors duration-300 ${
                            twConnected ? 'bg-gray-400 text-white cursor-not-allowed' : 'bg-blue-400 text-white hover:bg-blue-500'
                        }`}
                        onClick={twConnected ? undefined : onConnectTwitter}
                        disabled={twConnected || twStatusLoading}
                    >
                        <svg className="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 24 24">
                            <path d="M22.46 6c-.8.36-1.64.6-2.5.7.9-.54 1.5-1.4 1.8-2.4-.8.5-1.7.9-2.7 1.1-.8-.8-1.9-1.3-3.2-1.3-2.4 0-4.3 1.9-4.3 4.3 0 .34.04.67.1.98-3.6-.18-6.8-1.9-9-4.5-.4.6-.6 1.4-.6 2.2 0 1.5.8 2.8 2 3.6-.7 0-1.4-.2-2-.5v.05c0 2.1 1.5 3.8 3.5 4.2-.3.08-.7.13-1 .13-.24 0-.47-.02-.7-.07.5 1.7 2.1 2.9 4 2.9-1.5 1.2-3.4 1.9-5.5 1.9-.36 0-.7-.02-1.04-.06C2.9 20.3 5.3 21 7.9 21c9.4 0 14.5-7.8 14.5-14.5 0-.22-.01-.44-.02-.66z" />
                        </svg>
                        {twStatusLoading ? 'Checking...' : twConnected ? 'Twitter Connected' : 'Connect Twitter'}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default SocialConnections;