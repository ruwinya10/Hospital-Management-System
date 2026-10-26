export const prefixes={patients:'P',doctors:'D',departments:'DEP',appointments:'APT',admissions:'ADM','medical-records':'MR','laboratory-tests':'LAB',pharmacy:'MED',bills:'B',payments:'PAY',employees:'E',attendance:'ATT','leave-records':'LV',documents:'DOC',users:'USR',audit:'AUD',movements:'MOV'};
export const referenceModules={patientId:'patients',doctorId:'doctors',departmentId:'departments',appointmentId:'appointments',billId:'bills',employeeId:'employees',medicineId:'pharmacy'};
export function recordId(module,id){return id===null||id===undefined||id===''?'—':/^\d+$/.test(String(id))?(prefixes[module]||'R')+String(id).padStart(4,'0'):String(id)}
export function detailsOf(record){try{return JSON.parse(record.details||'{}')}catch{return {}}}
export function availability(value){try{const slots=JSON.parse(value||'[]');return Array.isArray(slots)?slots:[]}catch{return []}}
export function slotsForDate(schedule,date){if(!/^\d{4}-\d{2}-\d{2}$/.test(date||''))return [];const day=new Intl.DateTimeFormat('en-US',{weekday:'long',timeZone:'UTC'}).format(new Date(date+'T12:00:00Z'));return availability(schedule).filter(s=>s.day===day).sort((a,b)=>a.start.localeCompare(b.start))}

export const entityModules={Patient:'patients',Doctor:'doctors',Department:'departments',Appointment:'appointments',Admission:'admissions',MedicalRecord:'medical-records',LaboratoryTest:'laboratory-tests',PharmacyItem:'pharmacy',Bill:'bills',Payment:'payments',Employee:'employees',Attendance:'attendance',LeaveRecord:'leave-records',Document:'documents',UserAccount:'users'};

export function patientFileError(files){return files.some(file=>!/[.](docx?|pdf|png|jpe?g)$/i.test(file.name))?"Supported files: Word (.doc, .docx), PDF, PNG, JPG and JPEG.":files.some(file=>file.size===0||file.size>5*1024*1024)?"Each document must be non-empty and no larger than 5 MB.":""}
