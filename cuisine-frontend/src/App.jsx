import './App.css'
import {BrowserRouter, Route, Routes} from 'react-router-dom'
import MainPage from './components/mainpage'
import PostComponent from './components/PostComponent'
import Login from './components/Login'
import Register from './components/Register'
import Navbar from './components/Navbar'

function App() {
  
  return (
    <>
      <BrowserRouter>

        <Navbar/>

        <Routes>

          <Route path='/' element = { <MainPage />}></Route>

          <Route path='/add-post' element = { <PostComponent />}></Route>

          <Route path='/modify-post/:postId' element = { <PostComponent />}></Route>

          <Route path='/login' element = { <Login/>}></Route>

          <Route path='/register' element = { <Register />}></Route>
        </Routes>

      </BrowserRouter>
    </>
  )
}

export default App