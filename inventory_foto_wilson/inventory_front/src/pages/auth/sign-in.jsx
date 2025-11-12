import {
  Card,
  Input,
  Checkbox,
  Button,
  Typography,
} from "@material-tailwind/react";
import { Link, useNavigate } from "react-router-dom";
import { useState } from "react";
import axios from "axios";

export function SignIn() {
  const [userName, setUserName] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    try {
      const res = await axios.post(
        "http://localhost:8080/auth/login",
        { userName, password },
        { withCredentials: true } 
      );

      console.log("Login exitoso:", res.data);
      // Redirige al dashboard
      navigate("/dashboard");
    } catch (err) {
      console.error("Error de login:", err);
      setError("Credenciales inválidas o error del servidor");
    }
  };

  return (
    <section className="m-8 flex gap-4">
      <div className="w-full lg:w-3/5 mt-24">
        <div className="text-center">
          <Typography variant="h2" className="font-bold mb-4">
            Iniciar Sesión
          </Typography>
          <Typography
            variant="paragraph"
            color="blue-gray"
            className="text-lg font-normal"
          >
            Ingresa tu usuario y contraseña para continuar.
          </Typography>
        </div>

        <form
          onSubmit={handleSubmit}
          className="mt-8 mb-2 mx-auto w-80 max-w-screen-lg lg:w-1/2"
        >
          <div className="mb-1 flex flex-col gap-6">
            <Typography
              variant="small"
              color="blue-gray"
              className="-mb-3 font-medium"
            >
              Usuario
            </Typography>
            <Input
              size="lg"
              placeholder="admin"
              value={userName}
              onChange={(e) => setUserName(e.target.value)}
              className="!border-t-blue-gray-200 focus:!border-t-gray-900"
              labelProps={{
                className: "before:content-none after:content-none",
              }}
            />

            <Typography
              variant="small"
              color="blue-gray"
              className="-mb-3 font-medium"
            >
              Contraseña
            </Typography>
            <Input
              type="password"
              size="lg"
              placeholder="********"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="!border-t-blue-gray-200 focus:!border-t-gray-900"
              labelProps={{
                className: "before:content-none after:content-none",
              }}
            />
          </div>

          {error && (
            <Typography color="red" className="text-center text-sm mt-2">
              {error}
            </Typography>
          )}

          <Checkbox
            label={
              <Typography
                variant="small"
                color="gray"
                className="flex items-center justify-start font-medium"
              >
                Recordarme
              </Typography>
            }
            containerProps={{ className: "-ml-2.5" }}
          />

          <Button type="submit" className="mt-6" fullWidth>
            Sign In
          </Button>

          <Typography
            variant="paragraph"
            className="text-center text-blue-gray-500 font-medium mt-4"
          >
            ¿No tienes cuenta?
            <Link to="/auth/sign-up" className="text-gray-900 ml-1">
              Crear cuenta
            </Link>
          </Typography>
        </form>
      </div>

      <div className="w-2/5 h-full hidden lg:block">
        <img
          src="/img/pattern.png"
          className="h-full w-full object-cover rounded-3xl"
        />
      </div>
    </section>
  );
}

export default SignIn;
