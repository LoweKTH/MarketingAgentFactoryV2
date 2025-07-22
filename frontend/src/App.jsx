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
                        {/* Protect the /content route using PrivateRoute */}
                        <Route
                            path="/content"
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
                    </Routes>
                    <Footer /> {/* Footer also needs to be inside Router if it uses Link/useNavigate */}
                </Router>
            </AuthProvider>
        </div>
    );
}

export default App;
