'use client';

import { ReactNode, useMemo, useState } from 'react';
import {
  AppBar,
  Avatar,
  Box,
  Divider,
  Drawer,
  IconButton,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Toolbar,
  Typography,
  Menu,
  MenuItem,
  Stack,
  Chip,
} from '@mui/material';
import {
  Menu as MenuIcon,
  Dashboard,
  People,
  Assignment,
  Upload,
  Download,
  Inbox,
  AdminPanelSettings,
  Business,
  Notifications,
  AccountCircle,
  Logout,
} from '@mui/icons-material';
import { useRouter, usePathname } from 'next/navigation';
import { useAuth } from '@/providers/AuthProvider';
import { UserRole } from '@/types';

const DRAWER_WIDTH = 260;

interface MenuItemLink {
  text: string;
  icon: ReactNode;
  path: string;
  roles: UserRole[];
}

const menuItems: MenuItemLink[] = [
  { text: 'Dashboard', icon: <Dashboard />, path: '/recruiter/dashboard', roles: [UserRole.RECRUITER] },
  { text: 'Заявки', icon: <Assignment />, path: '/recruiter/applications', roles: [UserRole.RECRUITER] },
  { text: 'Импорт', icon: <Upload />, path: '/recruiter/import', roles: [UserRole.RECRUITER] },
  { text: 'Экспорт', icon: <Download />, path: '/recruiter/export', roles: [UserRole.RECRUITER] },
  { text: 'HR Dashboard', icon: <People />, path: '/hr', roles: [UserRole.RECRUITER, UserRole.ADMIN] },
  { text: 'Входящие', icon: <Inbox />, path: '/hm/inbox', roles: [UserRole.HM] },
  { text: 'Программы', icon: <Business />, path: '/admin/programs', roles: [UserRole.ADMIN, UserRole.RECRUITER] },
  { text: 'Шаблоны', icon: <Notifications />, path: '/admin/templates', roles: [UserRole.ADMIN] },
  { text: 'Пользователи', icon: <People />, path: '/admin/users', roles: [UserRole.ADMIN] },
  { text: 'Аудит', icon: <AdminPanelSettings />, path: '/admin/audit', roles: [UserRole.ADMIN] },
];

interface DashboardLayoutProps {
  children: ReactNode;
}

export default function DashboardLayout({ children }: DashboardLayoutProps) {
  const router = useRouter();
  const pathname = usePathname();
  const { user, logout } = useAuth();
  const [mobileOpen, setMobileOpen] = useState(false);
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);

  const filteredMenuItems = useMemo(
    () => menuItems.filter((item) => item.roles.some((role) => user?.roles.includes(role))),
    [user?.roles]
  );

  const handleDrawerToggle = () => {
    setMobileOpen(!mobileOpen);
  };

  const handleMenuClick = (path: string) => {
    router.push(path);
    setMobileOpen(false);
  };

  const handleProfileMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleProfileMenuClose = () => {
    setAnchorEl(null);
  };

  const handleLogout = () => {
    handleProfileMenuClose();
    logout();
    router.push('/login');
  };

  const drawer = (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <Toolbar sx={{ px: 3, py: 2 }}>
        <Stack spacing={0.5}>
          <Typography variant="subtitle2" color="text.secondary">
            X5 Recruitment
          </Typography>
          <Typography variant="h6" sx={{ color: '#fff' }}>
            Control Center
          </Typography>
        </Stack>
      </Toolbar>
      <Divider sx={{ borderColor: 'rgba(255,255,255,0.06)' }} />
      <List sx={{ flex: 1, py: 1 }}>
        {filteredMenuItems.map((item) => (
          <ListItem key={item.path} disablePadding>
            <ListItemButton
              selected={pathname === item.path}
              onClick={() => handleMenuClick(item.path)}
              sx={{
                px: 3,
                py: 1.25,
                borderRadius: 2,
                mx: 1,
                color: '#e2e8f0',
                transition: 'all 180ms ease',
                '&.Mui-selected': {
                  background: 'linear-gradient(135deg, rgba(79,70,229,0.35), rgba(14,165,233,0.32))',
                  color: '#fff',
                  boxShadow: '0 10px 30px rgba(0,0,0,0.24)',
                },
                '&:hover': {
                  backgroundColor: 'rgba(255,255,255,0.08)',
                },
              }}
            >
              <ListItemIcon sx={{ color: 'inherit', minWidth: 36 }}>{item.icon}</ListItemIcon>
              <ListItemText primary={item.text} primaryTypographyProps={{ fontWeight: 700 }} />
            </ListItemButton>
          </ListItem>
        ))}
      </List>
      <Box sx={{ px: 3, pb: 2 }}>
        <Chip
          label={user?.roles.join(', ')}
          size="small"
          sx={{
            color: '#e2e8f0',
            borderColor: 'rgba(255,255,255,0.15)',
            backgroundColor: 'rgba(255,255,255,0.05)',
          }}
          variant="outlined"
        />
      </Box>
    </Box>
  );

  return (
    <Box
      sx={{
        display: 'flex',
        minHeight: '100vh',
        background: 'linear-gradient(180deg, #f7f9ff 0%, #eef2f7 100%)',
      }}
    >
      <AppBar
        position="fixed"
        color="inherit"
        sx={{
          width: { sm: `calc(100% - ${DRAWER_WIDTH}px)` },
          ml: { sm: `${DRAWER_WIDTH}px` },
          backdropFilter: 'blur(10px)',
          backgroundColor: 'rgba(255,255,255,0.92)',
          borderBottom: '1px solid rgba(148, 163, 184, 0.35)',
        }}
      >
        <Toolbar sx={{ minHeight: 72, px: { xs: 2, md: 4 } }}>
          <IconButton
            color="inherit"
            aria-label="open drawer"
            edge="start"
            onClick={handleDrawerToggle}
            sx={{ mr: 2, display: { sm: 'none' } }}
          >
            <MenuIcon />
          </IconButton>
          <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
            {filteredMenuItems.find((item) => item.path === pathname)?.text || 'Dashboard'}
          </Typography>
          <Stack direction="row" spacing={1.5} alignItems="center">
            <Typography variant="body2" color="text.secondary">
              {user?.fullName}
            </Typography>
            <IconButton onClick={handleProfileMenuOpen} color="inherit" sx={{ border: '1px solid', borderColor: 'divider' }}>
              <Avatar sx={{ width: 36, height: 36 }}>
                <AccountCircle />
              </Avatar>
            </IconButton>
          </Stack>
          <Menu anchorEl={anchorEl} open={Boolean(anchorEl)} onClose={handleProfileMenuClose}>
            <MenuItem onClick={handleLogout}>
              <ListItemIcon>
                <Logout fontSize="small" />
              </ListItemIcon>
              Выйти
            </MenuItem>
          </Menu>
        </Toolbar>
      </AppBar>
      <Box component="nav" sx={{ width: { sm: DRAWER_WIDTH }, flexShrink: { sm: 0 } }}>
        <Drawer
          variant="temporary"
          open={mobileOpen}
          onClose={handleDrawerToggle}
          ModalProps={{ keepMounted: true }}
          sx={{
            display: { xs: 'block', sm: 'none' },
            '& .MuiDrawer-paper': { boxSizing: 'border-box', width: DRAWER_WIDTH },
          }}
        >
          {drawer}
        </Drawer>
        <Drawer
          variant="permanent"
          sx={{
            display: { xs: 'none', sm: 'block' },
            '& .MuiDrawer-paper': { boxSizing: 'border-box', width: DRAWER_WIDTH },
          }}
          open
        >
          {drawer}
        </Drawer>
      </Box>
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          p: { xs: 2.5, md: 4 },
          width: { sm: `calc(100% - ${DRAWER_WIDTH}px)` },
        }}
      >
        <Toolbar />
        <Box sx={{ maxWidth: 1440, mx: 'auto', width: '100%', display: 'grid', gap: 3 }}>{children}</Box>
      </Box>
    </Box>
  );
}
