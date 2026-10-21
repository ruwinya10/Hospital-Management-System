import {defineConfig,loadEnv} from 'vite';
import react from '@vitejs/plugin-react';
export default defineConfig(({mode})=>{
 const env=loadEnv(mode,process.cwd(),'');
 if(process.env.VERCEL){
  const address=env.VITE_API_BASE_URL;
  if(!address)throw new Error('Set VITE_API_BASE_URL to https://YOUR-BACKEND.onrender.com/api in Vercel.');
  let url;try{url=new URL(address)}catch{throw new Error('VITE_API_BASE_URL must be a valid HTTPS API address.')}
  if(url.protocol!=='https:'||url.username||url.password||url.search||url.hash||url.pathname.replace(/\/+$/,'')!=='/api')throw new Error('VITE_API_BASE_URL must be https://YOUR-BACKEND.onrender.com/api, without credentials or query parameters.');
 }
 return {plugins:[react()],server:{proxy:{'/api':'http://127.0.0.1:8080'}}};
});
