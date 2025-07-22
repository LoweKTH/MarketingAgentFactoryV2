import React from 'react';

const SocialConnections = ({ fbConnected, fbStatusLoading, onConnectFacebook }) => {
    return (
        <div className="bg-white p-6 rounded-xl shadow-lg hover:shadow-xl transition-shadow duration-300">
            <h2 className="text-2xl font-bold text-gray-800 mb-4 flex items-center">
                <svg className="w-6 h-6 mr-2 text-blue-600" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M12 2C6.477 2 2 6.477 2 12s4.477 10 10 10 10-4.477 10-10S17.523 2 12 2zm3.5 8h-3v8h-2v-8h-3V7h8v3z" />
                </svg>
                Social Connections
            </h2>
            <p className="text-gray-600 mb-4">Connect your social media accounts.</p>
            <div className="space-y-3">
                <button
                    className={`w-full py-2 px-4 rounded-lg flex items-center justify-center shadow-md transition-colors duration-300 ${
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
                <button className="w-full bg-blue-400 text-white py-2 px-4 rounded-lg hover:bg-blue-500 transition-colors duration-300 flex items-center justify-center shadow-md">
                    <svg className="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 24 24">
                        <path d="M22.46 6c-.8.36-1.64.6-2.5.7.9-.54 1.5-1.4 1.8-2.4-.8.5-1.7.9-2.7 1.1-.8-.8-1.9-1.3-3.2-1.3-2.4 0-4.3 1.9-4.3 4.3 0 .34.04.67.1.98-3.6-.18-6.8-1.9-9-4.5-.4.6-.6 1.4-.6 2.2 0 1.5.8 2.8 2 3.6-.7 0-1.4-.2-2-.5v.05c0 2.1 1.5 3.8 3.5 4.2-.3.08-.7.13-1 .13-.24 0-.47-.02-.7-.07.5 1.7 2.1 2.9 4 2.9-1.5 1.2-3.4 1.9-5.5 1.9-.36 0-.7-.02-1.04-.06C2.9 20.3 5.3 21 7.9 21c9.4 0 14.5-7.8 14.5-14.5 0-.22-.01-.44-.02-.66z" />
                    </svg>
                    Connect Twitter
                </button>
                <button className="w-full bg-red-600 text-white py-2 px-4 rounded-lg hover:bg-red-700 transition-colors duration-300 flex items-center justify-center shadow-md">
                    <svg className="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 24 24">
                        <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm5.4 14.5c-.2 0-.4-.02-.6-.07-.9-.2-2.3-.5-3.8-.5-1.5 0-2.9.3-3.8.5-.2.05-.4.07-.6.07-.4 0-.7-.3-.7-.7v-2.2c0-.4.3-.7.7-.7.2 0 .4.02.6.07.9.2 2.3.5 3.8.5 1.5 0 2.9-.3 3.8-.5.2-.05.4-.07.6-.07.4 0 .7.3.7.7v2.2c0 .4-.3.7-.7.7zM12 10.5c-1.7 0-3.1-1.4-3.1-3.1S10.3 4.3 12 4.3s3.1 1.4 3.1 3.1-1.4 3.1-3.1 3.1z" />
                    </svg>
                    Connect Google
                </button>
            </div>
        </div>
    );
};

export default SocialConnections;