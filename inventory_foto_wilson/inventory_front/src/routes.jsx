import {
  HomeIcon,
  UserCircleIcon,
  TableCellsIcon,
  ServerStackIcon,
  ShoppingBagIcon,
  PlusCircleIcon,
  PencilIcon,
  DocumentTextIcon,
  ChartBarIcon,
  UsersIcon,
} from "@heroicons/react/24/solid";
import { Home, Profile } from "@/pages/dashboard";
import { SignIn } from "@/pages/auth";
import { Products, CreateProduct, EditProduct } from "@/pages/dashboard/product";
import { Sales, Alerts } from "@/pages/dashboard/sale";
import { DpiDocuments } from "@/pages/dashboard/documents";
import { Reports } from "@/pages/dashboard/reports";
import { Users } from "@/pages/dashboard/users";

const icon = {
  className: "w-5 h-5 text-inherit",
};

export const routes = [
  {
    layout: "dashboard",
    pages: [
      {
        icon: <HomeIcon {...icon} />,
        name: "dashboard",
        path: "/home",
        element: <Home />,
        roles: ["ROLE_ADMIN"],
      },
      {
        icon: <UserCircleIcon {...icon} />,
        name: "Perfil",
        path: "/profile",
        element: <Profile />,
        roles: ["ROLE_ADMIN", "ROLE_EMPLOYEE"],
      },
      {
        icon: <ShoppingBagIcon {...icon} />,
        name: "Productos",
        path: "/products",
        element: <Products />,
        roles: ["ROLE_ADMIN", "ROLE_EMPLOYEE"],
      },
      {
        icon: <PlusCircleIcon {...icon} />,
        name: "Nuevo Producto",
        path: "/products/create",
        element: <CreateProduct />,
        roles: ["ROLE_ADMIN", "ROLE_EMPLOYEE"],
        hidden: true,
      },
      {
        icon: <PencilIcon {...icon} />,
        name: "Editar Producto",
        path: "/products/edit/:id",
        element: <EditProduct />,
        roles: ["ROLE_ADMIN", "ROLE_EMPLOYEE"],
        hidden: true,
      },
       {
        icon: <TableCellsIcon {...icon} />,
        name: "Ventas",
        path: "/sales",
        element: <Sales />,
        roles: ["ROLE_ADMIN", "ROLE_EMPLOYEE"],
      },
      {
        icon: <TableCellsIcon {...icon} />,
        name: "Alertas",
        path: "/alerts",
        element: <Alerts />,
        roles: ["ROLE_ADMIN", "ROLE_EMPLOYEE"],
      },
      {
        icon: <DocumentTextIcon {...icon} />,
        name: "Documentos DPI",
        path: "/documents/dpi",
        element: <DpiDocuments />,
        roles: ["ROLE_ADMIN", "ROLE_EMPLOYEE"],
      },
      {
        icon: <ChartBarIcon {...icon} />,
        name: "Reportes",
        path: "/reports",
        element: <Reports />,
        roles: ["ROLE_ADMIN", "ROLE_EMPLOYEE"],
      },
      {
        icon: <UsersIcon {...icon} />,
        name: "Usuarios",
        path: "/users",
        element: <Users />,
        roles: ["ROLE_ADMIN"],
      },
    ],
  },
  {
    title: "auth pages",
    layout: "auth",
    hidden: true,
    pages: [
      {
        icon: <ServerStackIcon {...icon} />,
        name: "sign in",
        path: "/sign-in",
        element: <SignIn />,
      },
    ],
  },
];

export default routes;
