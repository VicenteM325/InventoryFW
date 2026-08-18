import {
  Card,
  CardBody,
  Avatar,
  Typography,
  Tooltip,
  Button,
} from "@material-tailwind/react";
import { PencilIcon } from "@heroicons/react/24/solid";
import { ProfileInfoCard } from "@/widgets/cards";
import { useState, useEffect } from "react";
import { getUserDetails } from "../../services/authService";

export function Profile() {
  const [userData, setUserData] = useState({
    name: "",
    userName: "",
    email: "",
    role: null,
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchUserData = async () => {
      try {
        setLoading(true);
        const response = await getUserDetails();
        console.log("Datos del usuario:", response);
        
        setUserData({
          name: response.name || "",
          userName: response.userName || "",
          email: response.email || "",
          role: response.role || null,
        });
        setError(null);
      } catch (err) {
        console.error("Error al cargar datos del usuario:", err);
        setError("No se pudieron cargar los datos del usuario");
      } finally {
        setLoading(false);
      }
    };

    fetchUserData();
  }, []);

  if (loading) {
    return (
      <div className="flex justify-center items-center h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex justify-center items-center h-screen">
        <Typography color="red" className="text-center">
          {error}
        </Typography>
      </div>
    );
  }

  const getRoleName = () => {
    if (userData.role && userData.role.name) {
      return userData.role.name;
    }
    return "Usuario";
  };

  const getInitials = () => {
    if (userData.name) {
      const names = userData.name.split(" ");
      if (names.length >= 2) {
        return `${names[0][0]}${names[1][0]}`.toUpperCase();
      }
      return names[0][0].toUpperCase();
    }
    if (userData.userName) {
      return userData.userName[0].toUpperCase();
    }
    return "U";
  };

  const profileDetails = {
    "Nombre completo": userData.name || "No especificado",
    "Nombre de usuario": userData.userName || "No especificado",
    Email: userData.email || "No especificado",
    Rol: getRoleName(),
  };

  return (
    <>
      <div className="relative mt-8 h-72 w-full overflow-hidden rounded-xl bg-gradient-to-r from-blue-500 to-purple-600">
        <div className="absolute inset-0 h-full w-full bg-black/40" />
      </div>
      <Card className="mx-3 -mt-16 mb-6 lg:mx-4 border border-blue-gray-100 shadow-xl">
        <CardBody className="p-6">
          <div className="mb-8 flex items-center gap-6">
            <div className="flex h-24 w-24 items-center justify-center rounded-full bg-gradient-to-r from-blue-500 to-blue-600 text-3xl font-bold text-white shadow-lg shadow-blue-500/40">
              {getInitials()}
            </div>
            <div>
              <Typography variant="h4" color="blue-gray" className="mb-1">
                {userData.name || userData.userName || "Usuario"}
              </Typography>
              <Typography
                variant="small"
                className="font-normal text-blue-gray-600"
              >
                {getRoleName()}
              </Typography>
            </div>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <ProfileInfoCard
              title="Información Personal"
              description={`Bienvenido, ${userData.name || userData.userName || "Usuario"}`}
              details={profileDetails}
              action={
                <Tooltip content="Editar Perfil">
                  <PencilIcon className="h-4 w-4 cursor-pointer text-blue-gray-500 hover:text-blue-500 transition-colors" />
                </Tooltip>
              }
            />
            
            <Card className="border border-blue-gray-100 shadow-none">
              <CardBody className="p-4">
                <Typography variant="h6" color="blue-gray" className="mb-4">
                  Actividad Reciente
                </Typography>
                <div className="space-y-4">
                  <div className="flex items-center gap-3 p-3 rounded-lg bg-blue-50">
                    <div className="h-2 w-2 rounded-full bg-blue-500"></div>
                    <div>
                      <Typography variant="small" className="font-medium">
                        Último acceso
                      </Typography>
                      <Typography variant="small" className="text-blue-gray-500">
                        Hoy a las {new Date().toLocaleTimeString()}
                      </Typography>
                    </div>
                  </div>
                  <div className="flex items-center gap-3 p-3 rounded-lg bg-gray-50">
                    <div className="h-2 w-2 rounded-full bg-green-500"></div>
                    <div>
                      <Typography variant="small" className="font-medium">
                        Estado
                      </Typography>
                      <Typography variant="small" className="text-blue-gray-500">
                        Activo
                      </Typography>
                    </div>
                  </div>
                </div>
              </CardBody>
            </Card>
          </div>
        </CardBody>
      </Card>
    </>
  );
}

export default Profile;