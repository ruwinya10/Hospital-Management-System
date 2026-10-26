import React,{useState} from 'react';
import {Eye,EyeOff} from 'lucide-react';
export default function PasswordInput(props){
 const [visible,setVisible]=useState(false);
 return <div className="relative"><input {...props} type={visible?'text':'password'} className={'pr-12 '+(props.className||'')}/><button type="button" className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-500" aria-label={visible?'Hide password':'Show password'} aria-pressed={visible} onClick={()=>setVisible(!visible)}>{visible?<EyeOff size={18}/>:<Eye size={18}/>}</button></div>;
}
