import React,{useEffect,useState} from 'react';
const targets={patientId:'patients',doctorId:'doctors',departmentId:'departments',employeeId:'employees',appointmentId:'appointments',billId:'bills'};
export function ReferenceInput({field,value,onChange,api,id,required,onDoctor}){
 const [records,setRecords]=useState([]),[query,setQuery]=useState(''),[open,setOpen]=useState(false),[error,setError]=useState('');
 useEffect(()=>{let active=true;api('/lookups/'+targets[field]).then(rows=>{if(active)setRecords(rows)}).catch(e=>{if(active)setError(e.status===404?'Name search is unavailable: the backend is missing the lookup endpoint. Deploy the latest backend version on Render, then reload this page.':e.message)});return()=>{active=false}},[api,field]);
 const selected=records.find(r=>String(r.id)===String(value));
 useEffect(()=>{if(field==='doctorId')onDoctor?.(selected)},[selected,field,onDoctor]);
 return <><input id={id} autoComplete="off" required={required} value={open?query:selected?`${selected.name} (${selected.recordId})`:''} placeholder="Type a name to search" onFocus={()=>{setQuery('');setOpen(true)}} onChange={e=>{setQuery(e.target.value);onChange('');setOpen(true)}} onBlur={()=>setOpen(false)} aria-expanded={open}/>{open&&<div className="border rounded max-h-48 overflow-auto">{value&&<button type="button" className="block w-full text-left p-2" onMouseDown={e=>e.preventDefault()} onClick={()=>{onChange('');setQuery('');setOpen(false)}}>Clear selection</button>}{records.filter(r=>r.name.toLowerCase().includes(query.trim().toLowerCase())).map(r=><button type="button" key={r.id} className="block w-full text-left p-2 hover:bg-cyan-50" onMouseDown={e=>e.preventDefault()} onClick={()=>{onChange(String(r.id));setOpen(false)}}>{r.name} — {r.recordId}</button>)}{!records.some(r=>r.name.toLowerCase().includes(query.trim().toLowerCase()))&&<p className="p-2 text-sm">No matching records.</p>}</div>}{error&&<p role="alert" className="field-error">{error}</p>}</>;
}
export const isReference=key=>!!targets[key];
const days=['Monday','Tuesday','Wednesday','Thursday','Friday','Saturday','Sunday'];
export function AvailabilityInput({value,onChange}){
 let slots=[];try{const parsed=JSON.parse(value||'[]');if(Array.isArray(parsed))slots=parsed}catch{}
 const update=next=>onChange(JSON.stringify(next));
 return <div>{value&&slots.length===0&&value!=='[]'&&<p className="field-hint">Previous availability: {value}</p>}{slots.map((slot,i)=><div className="flex gap-2 mb-2 flex-wrap" key={i}><select aria-label="Day of week" value={slot.day} onChange={e=>update(slots.map((s,n)=>n===i?{...s,day:e.target.value}:s))}>{days.map(day=><option key={day}>{day}</option>)}</select><input aria-label="Start time" type="time" required value={slot.start} onChange={e=>update(slots.map((s,n)=>n===i?{...s,start:e.target.value}:s))}/><input aria-label="End time" type="time" required value={slot.end} onChange={e=>update(slots.map((s,n)=>n===i?{...s,end:e.target.value}:s))}/><button type="button" onClick={()=>update(slots.filter((_,n)=>n!==i))}>Remove</button></div>)}<button type="button" className="btn" onClick={()=>update([...slots,{day:'Monday',start:'',end:''}])}>Add availability</button></div>;
}
export function AvailabilityDetails({doctor}){
 if(!doctor)return null;let slots;try{slots=JSON.parse(doctor.schedule)}catch{}
 return <div className="mt-2 text-sm"><strong>Doctor availability</strong>{Array.isArray(slots)&&slots.length?slots.map((s,i)=><p key={i}>{s.day}: {s.start}–{s.end}</p>):<p>{doctor.schedule&&doctor.schedule!=='[]'?doctor.schedule:'No availability recorded.'}</p>}</div>;
}

export const departmentSuggestions=['Cardiology','Neurology','Orthopedics','Pediatrics','General Medicine','General Surgery','Obstetrics and Gynecology','Dermatology','ENT','Ophthalmology','Psychiatry','Radiology','Pathology','Emergency','Anesthesiology','Oncology','Urology','Nephrology','Pharmacy','Laboratory','Nursing','Administration','Human Resources','Finance'];
export const specializationSuggestions=['General Practitioner','Cardiologist','Neurologist','Orthopedic Surgeon','Pediatrician','General Physician','General Surgeon','Obstetrician and Gynecologist','Dermatologist','ENT Specialist','Ophthalmologist','Psychiatrist','Radiologist','Pathologist','Emergency Physician','Anesthesiologist','Oncologist','Urologist','Nephrologist'];
export const titleSuggestions={doctors:['Dr.','Prof.','Assoc. Prof.','Prof. Dr.'],patients:['Mr.','Mrs.','Ms.','Miss','Mx.','Master','Baby','Rev.'],employees:['Mr.','Mrs.','Ms.','Miss','Mx.','Dr.','Prof.','Rev.']};
export function SuggestedInput({suggestions,...props}){
 return <><input {...props} type="text" list={props.id+'-suggestions'}/><datalist id={props.id+'-suggestions'}>{suggestions.map(value=><option key={value} value={value}/>)}</datalist></>;
}
export function DepartmentInput({api,id,value,custom,onChange,required}){
 const [departments,setDepartments]=useState([]);
 useEffect(()=>{let active=true;api('/lookups/departments').then(rows=>{if(active)setDepartments(rows)}).catch(()=>{});return()=>{active=false}},[api]);
 const selected=departments.find(d=>String(d.id)===String(value));
 return <SuggestedInput id={id} required={required} maxLength={200} placeholder="Select or type a department" value={custom||selected?.name||''} suggestions={[...new Set([...departments.map(d=>d.name),...departmentSuggestions])]} onChange={e=>{const name=e.target.value,match=departments.find(d=>d.name.toLowerCase()===name.trim().toLowerCase());onChange(match?String(match.id):'',name)}}/>;
}
