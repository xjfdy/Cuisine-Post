import axios from "axios"
import AuthService from "./AuthService"
// ./ means current directory
// ../ means previous directory

const API_BASE_URL = `${import.meta.env.VITE_API_URL}/api/post`;

// Get all post
export const allPost = (pageNo = 0, pageSize = 5, query = '') => {
    return axios.get(API_BASE_URL, {
        params: {
            pageNo: pageNo,
            pageSize: pageSize,
            query: query
        }
    });
};

// create a new Post
export const createPost = (post) => {

    const headers = AuthService.getAuthHeader();
    console.log('Creating post with headers:', headers); // 🔍 添加这行
    console.log('Post data:', post); // 🔍 调试信息

    return axios.post(API_BASE_URL, post, {
        headers: headers
    });
};
// Arrow function wont be hoist
// hoist example;

// foo(); // ✅ foo() can be invoked
// function foo() {
//     console.log("Hello");
// }

// Temporal Dead Zone
// bar(); // ❌ will get an error: bar is not a function
// const bar = () => {
//     console.log("World");
// }

// Get a Post
export const getPost = (postId) => axios.get(API_BASE_URL + '/' + postId);

// Update a Post
export const updatePost = (postId, post) => {

     const headers = AuthService.getAuthHeader();
    console.log('Updating post with headers:', headers);

    return axios.put(API_BASE_URL + '/' + postId, post, {
        headers: headers
    });
};

// delete a post
export const deletePost = (postId) => {

    const headers = AuthService.getAuthHeader();
    console.log('Deleting post with headers:', headers);

    return axios.delete(API_BASE_URL + '/' + postId, {
        headers: headers
    });
};
