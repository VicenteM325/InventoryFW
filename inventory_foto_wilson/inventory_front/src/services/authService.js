import axios from "axios";

const API_URL = "http://localhost:8080/auth";

const login = async (userName, password) => {
  const response = await axios.post(
    `${API_URL}/login`,
    { userName, password },
    {
      withCredentials: true,
    }
  );
  return response.data;
};

const getUserDetails = async () => {
  const response = await axios.get(`${API_URL}/user/details`, {
    withCredentials: true,
  });
  return response.data;
};

export default {
  login,
  getUserDetails,
};
