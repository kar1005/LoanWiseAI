import api from './baseService';

const AUTH_ENDPOINT = '/application';

const loanApplicationService = {
  
  submitApplication: async (userData) => {
    try {
      const response = await api.get(`${AUTH_ENDPOINT}/submit`,userData);
      return response.data;
    } catch (error) {
      throw error.response ? error.response.data : new Error('Failed to submit application');
    }
  },
  
};

export default loanApplicationService;