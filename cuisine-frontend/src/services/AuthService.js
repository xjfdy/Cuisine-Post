import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/auth'

class AuthService {

    // await waits until axios sending POST request and getting the response, then run the next commend
    // await can only be used within async function
    async login(usernameOrEmail, password) {
        const response = await axios.post(`${API_BASE_URL}/login`, {
            usernameOrEmail, 
            password
        });

        if (response.data.token) {
            localStorage.setItem('user', JSON.stringify(response.data));
        }

        return response.data;
    }

    async register(username, email, password, fullName) {
        const response = await axios.post(`${API_BASE_URL}/register`, {
            username,
            email,
            password,
            fullName
        });

        return response.data;
    }
    
    // function logout() incorrect，cannot use function within a class
    // function means global function
    logout() {
        localStorage.removeItem('user');
    }

    getCurrentUser() {
        const user = localStorage.getItem('user');
        return user ? JSON.parse(user) : null;
    }

    // parse() example
    // const str = '{"token":"abc123","username":"john"}';
    // const obj = JSON.parse(str);
    // console.log(obj.token);     // "abc123"
    // console.log(obj.username);  // "john"


    getToken() {
        const user = this.getCurrentUser();
        return user ? user.token : null;
    }

    isLoggedIn() {
        return this.getCurrentUser() !== null;
    }

    getAuthHeader() {
        const token = this.getToken();
        return token ? {'Authorization': `Bearer ${token}`} : {};
    }
}

export default new AuthService();