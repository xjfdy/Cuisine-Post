import React, {useState} from 'react';
import { useNavigate, Link } from 'react-router-dom';
import AuthService from '../services/AuthService';

const Register = () => {
    const [username, setUsername] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [fullName, setFullName] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');

        if (password !== confirmPassword) {
            setError('Password does not match');
            return;
        }

        if (password.length < 6) {
            setError('Password must be at least 6 characters');
            return;
        }

        setLoading(true);

        try {
            await AuthService.register(username, email, password, fullName);
            alert('Registration successful! Please login');
            navigate('/login')
        } catch (err) {
            setError(err.response?.data?.message || 'Registration failed, please try again');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className='auth-container'>
            <div className='auth-card'>
                <div className='auth-header'>
                    <h2>✨ Register</h2>
                    <p>Join us and start your journey</p>
                </div>

                {error && (
                    <div className='alert alert-danger'>
                        {error}
                    </div>
                )}

                <form onSubmit={handleSubmit}>
                    <div className='form-group'>
                        <label>👤 Username</label>
                        <input
                            type='text'
                            className='form-control'
                            placeholder='Choose a username'
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            required
                        />
                    </div>

                    <div className='form-group'>
                        <label>📧 Email</label>
                        <input
                            type='email'
                            className='form-control'
                            placeholder='Enter email address'
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            required
                        />
                    </div>

                    <div className='form-group'>
                        <label>👨 Full Name</label>
                        <input
                            type='text'
                            className='form-control'
                            placeholder='Enter your full name'
                            value={fullName}
                            onChange={(e) => setFullName(e.target.value)}
                            required
                        />
                    </div>

                    <div className='form-group'>
                        <label>🔒 Password</label>
                        <input
                            type='password'
                            className='form-control'
                            placeholder='Set password (at least 6 characters)'
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                        />
                    </div>

                    <div className='form-group'>
                        <label>🔒 Confirm Password</label>
                        <input
                            type='password'
                            className='form-control'
                            placeholder='Enter password again'
                            value={confirmPassword}
                            onChange={(e) => setConfirmPassword(e.target.value)}
                            required
                        />
                    </div>

                    <button
                        type='submit'
                        className='btn btn-primary btn-block'
                        disabled={loading}
                    >
                        {loading ? 'Registering...' : 'Register'}
                    </button>

                </form>

                <div className='auth-footer'>
                    <p>Already have an account? <Link to='/login'>Login now</Link></p>
                </div>
            </div>
        </div>
    );
};

export default Register;