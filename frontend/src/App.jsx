// src/App.jsx
import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar'; // Global component
import Footer from './components/Footer'; // Global component
import LandingPage from './pages/LandingPage/LandingPage';
import ContentPage from './pages/ContentPage/ContentPage';
import LoginPage from './pages/LoginPage/LoginPage';
import PrivateRoute from './components/PrivateRoute'; // Import PrivateRoute
import ProfilePage from './pages/ProfilePage/ProfilePage';
import TestFacebookPostPage from './pages/ProfilePage/TestFacebookPostPage';
import TestTwitterPostPage from './pages/ProfilePage/TestTwitterPostPage';
import ConversationSelectionPage from './pages/ConversationSelectionPage/ConversationSelectionPage.jsx'; // Import the new page
import { AuthProvider } from './contexts/AuthContext'; // Import AuthProvider

function App() {
    return (
        <div className="min-h-screen bg-gray-50 font-sans antialiased text-gray-800">
            {/* AuthProvider wraps the entire Router to make authentication context available to all routes */}
            <AuthProvider>
                <Router>
                    <Navbar /> {/* Navbar needs to be inside Router to use Link/useNavigate */}
                    <Routes>
                        <Route path="/" element={<LandingPage />} />
                        <Route path="/login" element={<LoginPage />} />

                        {/* New route for selecting/starting conversations */}
                        <Route
                            path="/chat-select"
                            element={
                                <PrivateRoute>
                                    <ConversationSelectionPage />
                                </PrivateRoute>
                            }
                        />

                        {/* Updated route for ContentPage to accept an optional conversationId */}
                        {/* The '?' makes conversationId optional. E.g., /chat for new, /chat/123 for existing */}
                        <Route
                            path="/chat/:conversationId?"
                            element={
                                <PrivateRoute>
                                    <ContentPage />
                                </PrivateRoute>
                            }
                        />

                        <Route
                            path="/profile"
                            element={
                                <PrivateRoute>
                                    <ProfilePage />
                                </PrivateRoute>
                            }
                        />
                        <Route
                            path="/test-facebook-post"
                            element={
                                <PrivateRoute>
                                    <TestFacebookPostPage />
                                </PrivateRoute>
                            }
                        />
                        <Route
                            path="/test-twitter-post"
                            element={
                                <PrivateRoute>
                                    <TestTwitterPostPage />
                                </PrivateRoute>
                            }
                        />
                    </Routes>
                    <Footer /> {/* Footer also needs to be inside Router if it uses Link/useNavigate */}
                </Router>
            </AuthProvider>
        </div>
    );
}

export default App;