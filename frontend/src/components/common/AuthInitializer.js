// components/common/AuthInitializer.js
import { useEffect } from 'react';
import { useDispatch } from 'react-redux';
import { checkAuthState } from '../../redux/slices/authSlice';

function AuthInitializer() {
  const dispatch = useDispatch();
  
  useEffect(() => {
    // Check auth state on app load
    dispatch(checkAuthState());
  }, [dispatch]);
  
  // This component doesn't render anything
  return null;
}

export default AuthInitializer;