import {
  Card,
  Input,
  Checkbox,
  Button,
  Typography,
} from "@material-tailwind/react";
import { useNavigate } from "react-router-dom";
import { useState, useContext } from "react";
import { AuthContext } from "@/context/AuthContext";
import { login as loginService, getUserDetails } from "@/services/authService";
import { getHomeByRole } from "@/auth/menuByRole";

export function SignIn() {
  const [userName, setUserName] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const { login } = useContext(AuthContext);

  const handleSubmit = async (e) => {
  e.preventDefault();
  setError("");
  setLoading(true);

  try {
    // POST /auth/login solo responde { message: "<ROL>" } y deja la
    // sesión real en una cookie httpOnly (no en el cuerpo), así que el
    // objeto de usuario (nombre, userId, rol completo) se obtiene con una
    // segunda llamada, la misma que ya usa AuthContext.verifySession().
    await loginService(userName, password);
    const userDetails = await getUserDetails();

    login(userDetails);

    // Redirigir según el rol real ya guardado en el contexto
    const roleName = userDetails.role?.name || userDetails.role || '';
    navigate(getHomeByRole(roleName ? [roleName] : []));
      } catch (err) {
        console.error("Error de login:", err);
        setError(err.response?.data?.message || "Credenciales inválidas o error del servidor");
      } finally {
        setLoading(false);
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
              disabled={loading}
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
              disabled={loading}
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
            disabled={loading}
          />

          <Button type="submit" className="mt-6" fullWidth disabled={loading}>
            {loading ? "Iniciando sesión..." : "Iniciar Sesión"}
          </Button>

          <Typography
            variant="paragraph"
            className="text-center text-blue-gray-500 font-medium mt-4"
          >
            Sistema interno de Multiservicios Wilson. Si no tienes una cuenta, solicítala a un administrador.
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