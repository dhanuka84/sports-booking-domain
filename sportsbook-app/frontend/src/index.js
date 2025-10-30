import React from "react";
import ReactDOM from "react-dom/client";

const App = () => {
  return (
    <div style={{ padding: "2rem" }}>
      <h1>Sportsbook Platform UI</h1>
      <ul>
        <li><a href="http://localhost:8081/swagger-ui.html" target="_blank">Player Service Swagger</a></li>
        <li><a href="http://localhost:8082/swagger-ui.html" target="_blank">Wallet Service Swagger</a></li>
        <li><a href="http://localhost:8083/swagger-ui.html" target="_blank">Odds Service Swagger</a></li>
        <li><a href="http://localhost:8084/swagger-ui.html" target="_blank">Betting Service Swagger</a></li>
        <li><a href="http://localhost:8085/swagger-ui.html" target="_blank">Risk Service Swagger</a></li>
        <li><a href="http://localhost:8086/swagger-ui.html" target="_blank">Settlement Service Swagger</a></li>
      </ul>
    </div>
  );
};

const root = ReactDOM.createRoot(document.getElementById("root"));
root.render(<App />);
