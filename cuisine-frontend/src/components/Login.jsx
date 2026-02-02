import React, {useState} from 'react';
import { useNavigate, Link } from 'react-router-dom';
import AuthService from '../services/AuthService';

const Login = () => {
    const [usernameOrEmail, setUsernameOrEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            await AuthService.login(usernameOrEmail, password);
            navigate('/');
            window.location.reload();
        } catch (err) {
            setError(err.response?.data?.message || 'Login failed, please check your credentials');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className='auth-container'>
            <div className='auth-card'>
                <div className='auth-header'>
                    <h2>🔐 Login</h2>
                    <p>Welcome back! Please login to your account</p>
                </div>

                {error && (
                    <div className='alert alert-danger'>
                        {error}
                    </div>

                )}

                <form onSubmit={handleSubmit}>
                    <div className='form-group'>
                        <label>👤 Username or Email</label>
                        <input
                            type='text'
                            className='form-control'
                            placeholder='Enter username or email'
                            value={usernameOrEmail}
                            onChange={(e) => setUsernameOrEmail(e.target.value)}
                            required // required means, here cannot be empty
                        />
                    </div>

                    <div className='form-group'>
                        <label>🔒 Password</label>
                        <input
                            type='password'
                            className='form-control'
                            placeholder='Enter password'
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                        />
                    </div>

                    <button
                        type='submit'
                        className='btn btn-primary btn-block'
                        disabled={loading} //disable the button when loading is true
                    >
                        {loading ? 'Logging in...' : 'Login'}
                    </button>

                </form>

                <div className='auth-footer'>
                    <p>Don't have an account? <Link to='/register'>Register now</Link></p>
                </div>
            </div>
        </div>
    );
};

export default Login;