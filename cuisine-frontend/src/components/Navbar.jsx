import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import AuthService from '../services/AuthService';

const Navbar = () => {
    const navigate = useNavigate();
    const currentUser = AuthService.getCurrentUser();

    const handleLogout = () => {
        AuthService.logout();
        navigate('/login');
        window.location.reload();
    };

    return (
        <nav className='navbar-custom'>
            <div className='navbar-container'>
                <Link to='/' className='navbar-brand'>
                    🍳 Cuisine Blog
                </Link>
            

                <div className='navbar-menu'>
                    {currentUser ? (
                        // logged in, display username and logout
                        <>
                            <Link to='/' className='nav-link'>
                                🏠 Home
                            </Link>
                            
                            <Link to='/add-post' className='nav-link'>
                                ✏️ Compose
                            </Link>

                            <div className='user-menu'>
                                <span className='user-name'>
                                    👤 {currentUser.username}
                                </span>
                                <button
                                    onClick={handleLogout}
                                    className='btn btn-logout'
                                >
                                    🚪 Logout
                                </button>
                            </div>
                        </>
                    ) :(
                        //Not log in, display Login and Register
                        <>
                        <Link to='/login' className='nav-link'>
                                🔐 Login
                            </Link>
                            <Link to='/register' className='btn btn-register'>
                                ✨ Register
                            </Link>
                        </>
                    )}
                </div>
            </div>
        </nav>
    );
};

export default Navbar;