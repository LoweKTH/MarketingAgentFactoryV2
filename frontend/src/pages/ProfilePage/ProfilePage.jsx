import React, { useEffect, useState } from 'react';
import { getFacebookStatus, redirectToFacebookOAuth } from '../../api/authApi';
import SocialConnections from './components/SocialConnections';
import ServiceCard from './components/ServiceCard';

const ProfilePage = () => {
    // Facebook connection status
    const [fbConnected, setFbConnected] = useState(false);
    const [fbStatusLoading, setFbStatusLoading] = useState(true);

    useEffect(() => {
        const fetchStatus = async () => {
            setFbStatusLoading(true);
            try {
                const data = await getFacebookStatus();
                setFbConnected(!!data.connected);
            } catch (e) {
                setFbConnected(false);
            } finally {
                setFbStatusLoading(false);
            }
        };
        fetchStatus();
    }, []);

    const handleFacebookConnect = () => {
        redirectToFacebookOAuth();
    };

    return (
        <div className="container mx-auto p-4 sm:p-6 lg:p-8 flex flex-col items-center justify-center min-h-[calc(100vh-120px)]">
            <h1 className="text-4xl font-extrabold text-gray-900 mb-8 text-center">Your Profile</h1>
            <p className="text-lg text-gray-700 mb-10 text-center max-w-2xl">
                Manage your profile settings and connect to various services to enhance your experience.
            </p>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 w-full max-w-4xl">
                {/* Social Media Connections */}
                <SocialConnections
                    fbConnected={fbConnected}
                    fbStatusLoading={fbStatusLoading}
                    onConnectFacebook={handleFacebookConnect}
                />

                {/* Other Service Connections */}
                <ServiceCard
                    title="Other Services"
                    description="Integrate with other useful services."
                    icon={
                        <svg className="w-6 h-6 mr-2 text-purple-600" fill="currentColor" viewBox="0 0 24 24">
                            <path d="M12 2C6.477 2 2 6.477 2 12s4.477 10 10 10 10-4.477 10-10S17.523 2 12 2zm-1 15h2v-6h-2v6zm0-8h2V7h-2v2z" />
                        </svg>
                    }
                >
                    {/* Add buttons or content specific to Other Services here */}
                    <div>
                        {/* Example: */}
                        {/* <button className="w-full bg-gray-200 py-2 px-4 rounded-lg hover:bg-gray-300 transition-colors duration-300 flex items-center justify-center shadow-md">
              Connect to Another Service
            </button> */}
                    </div>
                </ServiceCard>

                {/* Profile Settings */}
                <ServiceCard
                    title="Profile Settings"
                    description="Update your personal information and preferences."
                    icon={
                        <svg className="w-6 h-6 mr-2 text-green-600" fill="currentColor" viewBox="0 0 24 24">
                            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 4c1.1 0 2 .9 2 2s-.9 2-2 2-2-.9-2-2 .9-2 2-2zm0 14c-2.7 0-5.2-1.4-6.6-3.7.1-.2.2-.4.4-.6 1.4-1.7 3.5-2.8 5.9-2.8s4.5 1.1 5.9 2.8c.2.2.3.4.4.6-1.4 2.3-3.9 3.7-6.6 3.7z" />
                        </svg>
                    }
                >
                    {/* Add buttons or content specific to Profile Settings here */}
                    <div>
                        {/* Example: */}
                        {/* <button className="w-full bg-gray-200 py-2 px-4 rounded-lg hover:bg-gray-300 transition-colors duration-300 flex items-center justify-center shadow-md">
              Edit Profile
            </button> */}
                    </div>
                </ServiceCard>
            </div>
        </div>
    );
};

export default ProfilePage;