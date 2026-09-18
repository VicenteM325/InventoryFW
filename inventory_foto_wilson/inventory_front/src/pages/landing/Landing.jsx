import React from "react";
import { Link } from "react-router-dom";
import { Typography, Button, Card, CardBody } from "@material-tailwind/react";
import {
  DevicePhoneMobileIcon,
  IdentificationIcon,
  DocumentCheckIcon,
  CameraIcon,
  BuildingLibraryIcon,
  PhotoIcon,
  MapPinIcon,
  PhoneIcon,
  ClockIcon,
} from "@heroicons/react/24/outline";
import { Navbar } from "@/widgets/layout";

const BUSINESS = {
  name: "Multiservicios Wilson",
  slogan: "Podrán imitarnos, pero jamás igualarnos",
  address: "Zona 1, frente al Parque Central, San Juan Ixcoy, Huehuetenango",
  phone: "5166-0707",
  schedule: [
    { days: "Domingo a Viernes", hours: "8:00 a.m. – 5:00 p.m." },
    { days: "Sábado", hours: "Cerrado" },
  ],
};

const SERVICES = [
  { name: "Venta de celulares y accesorios", icon: DevicePhoneMobileIcon },
  { name: "Fotocopia de DPI", icon: IdentificationIcon },
  { name: "Emplasticado de documentos", icon: DocumentCheckIcon },
  { name: "Fotografías tamaño cédula/pasaporte", icon: CameraIcon },
  { name: "Trámites de SAT", icon: BuildingLibraryIcon },
  { name: "Ampliación de fotografías", icon: PhotoIcon },
];

const NAV_ROUTES = [
  { name: "Servicios", path: "#servicios" },
  { name: "Horario y ubicación", path: "#contacto" },
];

const MAPS_URL = `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(BUSINESS.address)}`;

const LoginButton = ({ variant = "gradient", color, fullWidth }) => (
  <Link to="/auth/sign-in">
    <Button variant={variant} color={color} size="sm" fullWidth={fullWidth}>
      Iniciar sesión
    </Button>
  </Link>
);

export function Landing() {
  return (
    <div className="min-h-screen bg-white">
      <div className="container mx-auto px-4 pt-4">
        <Navbar brandName={BUSINESS.name} routes={NAV_ROUTES} action={<LoginButton fullWidth />} />
      </div>

      {/* Hero */}
      <section className="bg-gradient-to-b from-blue-gray-50/60 to-white px-6 py-20 text-center">
        <Typography variant="h1" color="blue-gray" className="mb-4 text-3xl md:text-5xl">
          {BUSINESS.name}
        </Typography>
        <Typography variant="lead" color="blue-gray" className="mx-auto max-w-2xl italic opacity-80">
          &ldquo;{BUSINESS.slogan}&rdquo;
        </Typography>
        <Typography className="mx-auto mt-4 max-w-xl text-blue-gray-600">
          Tu tienda de confianza en San Juan Ixcoy para celulares, accesorios y trámites de documentos.
        </Typography>
      </section>

      {/* Servicios */}
      <section id="servicios" className="px-6 py-16">
        <Typography variant="h3" color="blue-gray" className="mb-10 text-center">
          Nuestros servicios
        </Typography>
        <div className="mx-auto grid max-w-5xl grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {SERVICES.map(({ name, icon: Icon }) => (
            <Card key={name} className="border border-blue-gray-50 shadow-sm">
              <CardBody className="flex flex-col items-center gap-3 text-center">
                <div className="rounded-full bg-blue-gray-50 p-3">
                  <Icon className="h-8 w-8 text-blue-gray-700" />
                </div>
                <Typography variant="h6" color="blue-gray">
                  {name}
                </Typography>
              </CardBody>
            </Card>
          ))}
        </div>
      </section>

      {/* Horario y ubicación */}
      <section id="contacto" className="bg-blue-gray-50/50 px-6 py-16">
        <div className="mx-auto max-w-3xl">
          <Typography variant="h3" color="blue-gray" className="mb-8 text-center">
            Horario y ubicación
          </Typography>
          <div className="grid gap-6 sm:grid-cols-1 md:grid-cols-3">
            <div className="flex items-start gap-3">
              <MapPinIcon className="h-6 w-6 shrink-0 text-blue-gray-700" />
              <div>
                <Typography color="blue-gray" className="font-medium">
                  Dirección
                </Typography>
                <Typography color="gray" className="text-sm">
                  {BUSINESS.address}
                </Typography>
                <a
                  href={MAPS_URL}
                  target="_blank"
                  rel="noreferrer"
                  className="text-sm text-blue-500 hover:underline"
                >
                  Ver en Google Maps
                </a>
              </div>
            </div>

            <div className="flex items-start gap-3">
              <PhoneIcon className="h-6 w-6 shrink-0 text-blue-gray-700" />
              <div>
                <Typography color="blue-gray" className="font-medium">
                  Teléfono
                </Typography>
                <Typography color="gray" className="text-sm">
                  {BUSINESS.phone}
                </Typography>
              </div>
            </div>

            <div className="flex items-start gap-3">
              <ClockIcon className="h-6 w-6 shrink-0 text-blue-gray-700" />
              <div>
                <Typography color="blue-gray" className="font-medium">
                  Horario
                </Typography>
                {BUSINESS.schedule.map((s) => (
                  <Typography key={s.days} color="gray" className="text-sm">
                    {s.days}: {s.hours}
                  </Typography>
                ))}
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Acceso para personal */}
      <section className="px-6 py-16">
        <Card className="mx-auto max-w-2xl border-2 border-blue-gray-100 bg-blue-gray-50/40 shadow-none">
          <CardBody className="text-center">
            <Typography variant="h5" color="blue-gray" className="mb-2">
              ¿Eres parte del equipo?
            </Typography>
            <Typography color="gray" className="mx-auto mb-6 max-w-md">
              Ingresa con tu cuenta para gestionar el inventario, las ventas y los documentos del negocio.
            </Typography>
            <div className="flex justify-center">
              <LoginButton variant="outlined" color="blue-gray" />
            </div>
          </CardBody>
        </Card>
      </section>

      {/* Pie de página */}
      <footer className="border-t border-blue-gray-50 px-6 py-8 text-center">
        <Typography variant="h6" color="blue-gray">
          {BUSINESS.name}
        </Typography>
        <Typography variant="small" color="gray" className="italic">
          &ldquo;{BUSINESS.slogan}&rdquo;
        </Typography>
        <Typography variant="small" color="gray" className="mt-2">
          {BUSINESS.address} · {BUSINESS.phone}
        </Typography>
        <Typography variant="small" color="gray" className="mt-4">
          © {new Date().getFullYear()} {BUSINESS.name}
        </Typography>
      </footer>
    </div>
  );
}

export default Landing;
