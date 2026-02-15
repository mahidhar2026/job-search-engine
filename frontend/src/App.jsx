import { useMemo, useState } from "react";
import { searchJobs } from "./api"; 
import "./App.css";

function formatSalary(n){
  if (n == null || n == undefined || Number.isNaN(Number(n))) return "--";
  const x = Math.round(Number(n));
  return x.toLocaleString();

}

function splitSkills(skills){
  if(!skills) return [];
  const parts = skills.includes(";")? skills.split(";"):
                skills.includes(",")?skills.split(","):
                skills.split("");
  return parts.map(s=>s.trim()).filter(Boolean).slice(0,8);

}

export default function App(){
  const[q,setQ] = useState("");
  const[page,setPage] = useState(0);
  const[size, setSize]= useState(10);

  const[items,setItems] = useState([]);
  const[loading,setLoading] = useState(false);
  const[err,setErr]=useState("");
  const[lastQuery,setLastQuery]=useState("");

  //pagination 
  const canNext = items.length === size ;
  const canPrev = page>0;

  //runsearch()
  async function runSearch(nextPage){
    const query = q.trim();
    if (!query){
      setErr("Type a keyword( e.g, python , java, backend , data engineer).");
      setItems([]);
      return;
    }
    setLoading(true);
    setErr("");
    try{
      const data = await searchJobs(query,nextPage,size);
      setItems(Array.isArray(data)? data:[]);
      setPage(nextPage);
      setLastQuery(query);

    } catch (e) {
      setErr(e.message || "Request failed");
      setItems([]);
    } finally {
      setLoading(false);
    }
  }
 //searching new jobs
  function onSubmit(e){
    e.preventDefault();
    runSearch(0);
  }
 // what to display above results
  const resultLabel = useMemo(() => {
    if (!lastQuery)return "Search to see results.";
    if(loading) return "Searching..";
    if(err) return "Fix the error and retry.";
    if(items.length === 0) return `No results for "${lastQuery}".`;
    return `Showing ${items.length} results for "${lastQuery}".`;
  }, [lastQuery,loading,err,items.length]);

  return(
    <div className = "container">
      <div className="header">
        <div >
          <h1> Job Search</h1>
          <div className="sub"> openSearch retrieval + rule based ranking </div>
        </div>
      </div>

      <div className="panel">
        <form onSubmit = {onSubmit} className = "formRow">
          <input 
          className="input"
          value={q}
          onChange = {(e) => setQ(e.target.value)}
          placeholder = "Search jobs(python,java,sde,ml engineer...)"
          />
          <button className ="btn" type = "submit" disabled={loading}>
            {loading ? "Searching..": "Search"}
          </button>

          <div className="selectWrap">
            <span> Size</span>
            <select value ={size} onChange= {(e) => setSize(Number(e.target.value))}>
              <option value = {10}>10</option>
              <option value= {20}>20</option>
              <option value = {50}>50</option>
            </select>
          </div>
        </form>

        <div className="metaRow">
          <div>{resultLabel}</div>
          <div>Page: <b>{page}</b></div>
        </div>

        {err && <div className = "notice err">{err}</div>}
        <div className="pager">
          <button className ="btn" onClick ={() => runSearch(page - 1)} disabled= {loading || !canPrev}>
            Prev
          </button>
          <div className="pagePill"> page {page}</div>
          <button className="btn" onClick={() => runSearch(page +1)} disabled = {loading|| !canNext}>
            Next
          </button>
        </div>
      </div>  

      <div className="grid">
        {items.map((job,idx) =>(
          <div className="card" key= {idx}>
            <div className="cardTop">
              <h3 className="title">{job.title || "Untitled role"}</h3>
              <div className="salary">
                {formatSalary(job.minSalary)} - {formatSalary(job.maxSalary)}
              </div>
            </div>
            <div className="skills">{job.skills || "No description/ skills available."}</div>
            <div className="badges">
              {splitSkills(job.skills).map((t,i) => (
                <span className="badge" key ={i} > {t}</span>                
              ))}
            </div>
          </div>
          

        ))}
      </div>
      
    </div>
  );
  
}