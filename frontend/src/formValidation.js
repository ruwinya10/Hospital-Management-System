import rules from './formRules.json';
export {rules};
export const label=key=>rules.fields[key]?.label||key.replace(/([A-Z])/g,' $1').replace(/^./,c=>c.toUpperCase());
export function today(){return new Intl.DateTimeFormat('en-CA',{timeZone:'Asia/Colombo',year:'numeric',month:'2-digit',day:'2-digit'}).format(new Date())}
function validDate(value){if(!/^\d{4}-\d{2}-\d{2}$/.test(value))return false;const d=new Date(value+'T00:00:00Z');return !Number.isNaN(d.getTime())&&d.toISOString().slice(0,10)===value}
export function fieldError(key,value,rule={}){
 const v=String(value??'').trim(),name=key==='name'?'Name / Title':label(key);
 if(!v)return rule.required?`${name} is required.`:'';
 if(rule.maxLength&&v.length>rule.maxLength)return `${name} must be at most ${rule.maxLength} characters.`;
 if(rule.pattern&&!new RegExp(rule.pattern).test(v))return rule.patternMessage||`Enter a valid ${name.toLowerCase()}.`;
 if(rule.type==='number'){
  if(!/^\d+(?:\.\d+)?$/.test(v)||!Number.isFinite(Number(v)))return `${name} must be a valid positive number.`;
  const n=Number(v);if(rule.integer&&!Number.isSafeInteger(n))return `${name} must be a whole number.`;
  if(n<rule.min||n>rule.max)return `${name} must be between ${rule.min} and ${rule.max}.`;
  if(rule.decimals!==undefined&&(v.split('.')[1]?.length||0)>rule.decimals)return `${name} allows at most ${rule.decimals} decimal places.`;
 }
 if(rule.type==='tel'&&(!/^\+?[\d ()-]+$/.test(v)||(v.match(/\d/g)||[]).length<7||(v.match(/\d/g)||[]).length>15))return `${name} must contain 7–15 digits; +, spaces, parentheses and hyphens are allowed.`;
 if(rule.type==='email'&&!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v))return 'Enter a valid email address, such as name@example.com.';
 if(rule.options&&!rule.options.some(o=>o.toLowerCase()===v.toLowerCase()))return `Choose a valid ${name.toLowerCase()} from the list.`;
 if(rule.type==='date'&&!validDate(v))return `Enter a valid ${name.toLowerCase()}.`;
 if(rule.type==='time'&&!/^([01]\d|2[0-3]):[0-5]\d(?::[0-5]\d)?$/.test(v))return 'Enter a valid time in 24-hour format.';
 if(rule.type==='datetime-local'&&(!/^\d{4}-\d{2}-\d{2}T([01]\d|2[0-3]):[0-5]\d(?::[0-5]\d(?:\.\d{1,3})?)?$/.test(v)||!validDate(v.slice(0,10))))return `Enter a valid ${name.toLowerCase()} date and time.`;
 if(rule.noFuture&&(rule.type==='date'?v>today():new Date(v+'+05:30').getTime()>Date.now()))return `${name} cannot be in the future.`;
 if(key==='dateOfBirth'&&v<`${Number(today().slice(0,4))-130}${today().slice(4)}`)return 'Date of birth cannot be more than 130 years ago.';
 return '';
}
export function recordRules(module,key){const config=rules.modules[module];return {...(rules.fields[key]||rules.common[key]||{}),required:key==='name'||key==='status'||config.required.includes(key),...(key==='status'?{options:config.statuses}:{})}}
export function validateRecord(module,form){
 const errors={},config=rules.modules[module];if(!config)return {name:'Unknown record type.'};
 for(const key of new Set(['name','status',...config.required,...Object.keys(form)])){const error=fieldError(key,form[key],recordRules(module,key));if(error)errors[key]=error}
 if(config.humanName&&form.name?.trim()&&!/^[\p{L}\p{M} .’'-]{2,120}$/u.test(form.name.trim()))errors.name='Enter a person’s name using 2–120 letters, spaces, apostrophes, dots or hyphens.';
 const order=(start,end,message)=>{if(form[start]&&form[end]&&form[end]<form[start])errors[end]=message};
 order('admissionDate','dischargeDate','Discharge date must be on or after admission date.');order('startDate','endDate','End date must be on or after start date.');order('billDate','dueDate','Due date must be on or after bill date.');order('requestedAt','completedAt','Completion must be on or after the request time.');order('checkIn','checkOut','Check-out must be on or after check-in.');
 if(module==='attendance'){if(form.status==='ABSENT'&&(form.checkIn||form.checkOut))errors.checkIn='An absent record cannot have check-in or check-out times.';else if(form.status!=='ABSENT'&&form.status!=='ARCHIVED'&&!form.checkIn)errors.checkIn='Check-in is required for a present employee.';if(form.date>today())errors.date='Attendance date cannot be in the future.'}
 if(module==='admissions'&&form.status==='DISCHARGED'&&!form.dischargeDate)errors.dischargeDate='Enter the discharge date for a discharged patient.';
 if(module==='laboratory-tests'&&form.status==='COMPLETED'){if(!String(form.result||'').trim())errors.result='Enter the result before marking the test completed.';if(!form.completedAt)errors.completedAt='Enter the completion date and time.'}
 if(module==='appointments'&&form.status==='COMPLETED'&&form.date>today())errors.date='A future appointment cannot be marked completed.';
 if(module==='payments'&&form.method&&form.method!=='CASH'&&!String(form.reference||'').trim())errors.reference='Enter a reference for a non-cash payment.';
 const charges=['consultationCharge','laboratoryCharge','pharmacyCharge','admissionCharge'];if(module==='bills'&&charges.some(k=>String(form[k]??'').trim()!=='')){const sum=charges.reduce((s,k)=>s+Math.round(Number(form[k]||0)*100),0);if(Math.round(Number(form.total||0)*100)!==sum)errors.total='Total must equal consultation, laboratory, pharmacy and admission charges combined.'}
 return errors;
}
export function passwordError(value){if(!value||value.length<12||value.length>72||new TextEncoder().encode(value).length>72)return 'Use 12–72 characters (at most 72 UTF-8 bytes).';if(!/[A-Za-z]/.test(value)||!/[0-9]/.test(value))return 'Include at least one letter and one number in the password.';return ''}
export function usernameError(value){return /^[A-Za-z0-9][A-Za-z0-9._-]{2,49}$/.test(value||'')?'':'Use 3–50 letters, numbers, dots, underscores or hyphens; start with a letter or number.'}
export function focusError(errors){const first=Object.keys(errors)[0];requestAnimationFrame(()=>document.getElementById('record-'+first)?.focus())}
