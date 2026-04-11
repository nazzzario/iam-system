"use client";

import { useSession } from "next-auth/react";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import api from "@/lib/api";
import { hasRole } from "@/lib/jwt";
import { Navbar } from "@/components/navbar";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

interface UserRow {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  isActive: boolean;
  role: string;
}

interface NewUserForm {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  role: string;
}

const ROLE_BADGE: Record<string, string> = {
  USER: "bg-gray-100 text-gray-700",
  MANAGER: "bg-blue-100 text-blue-700",
  ADMIN: "bg-red-100 text-red-700",
};

function RoleBadge({ role }: { role: string }) {
  const classes = ROLE_BADGE[role] ?? ROLE_BADGE.USER;
  return (
    <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${classes}`}>
      {role}
    </span>
  );
}

const EMPTY_FORM: NewUserForm = {
  firstName: "",
  lastName: "",
  email: "",
  password: "",
  role: "USER",
};

export default function AdminPage() {
  const { data: session, status } = useSession();
  const router = useRouter();

  const [users, setUsers] = useState<UserRow[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState<NewUserForm>(EMPTY_FORM);
  const [formError, setFormError] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [actionLoading, setActionLoading] = useState<string | null>(null);

  const isAdmin = hasRole(session?.accessToken, "ADMIN");

  useEffect(() => {
    if (status === "unauthenticated") router.push("/");
  }, [status, router]);

  function fetchUsers() {
    return api
      .get<UserRow[]>("/api/v1/admin/users")
      .then((res) => setUsers(res.data))
      .catch(() => setError("Failed to load users."))
      .finally(() => setLoading(false));
  }

  useEffect(() => {
    if (status !== "authenticated" || !isAdmin) {
      setLoading(false);
      return;
    }
    fetchUsers();
  }, [status, isAdmin]);

  async function handleCreateUser() {
    setFormError("");
    setSubmitting(true);
    try {
      await api.post("/api/v1/admin/users", form);
      setModalOpen(false);
      setForm(EMPTY_FORM);
      setLoading(true);
      await fetchUsers();
    } catch {
      setFormError("Failed to create user.");
    } finally {
      setSubmitting(false);
    }
  }

  async function handleActivate(id: string) {
    setActionLoading(id + "_activate");
    try {
      await api.put(`/api/v1/admin/users/${id}/activate`);
      setUsers((prev) =>
        prev.map((u) => (u.id === id ? { ...u, isActive: true } : u))
      );
    } catch {
      setError("Failed to activate user.");
    } finally {
      setActionLoading(null);
    }
  }

  async function handleDeactivate(id: string) {
    setActionLoading(id + "_deactivate");
    try {
      await api.delete(`/api/v1/admin/users/${id}`);
      setUsers((prev) =>
        prev.map((u) => (u.id === id ? { ...u, isActive: false } : u))
      );
    } catch {
      setError("Failed to deactivate user.");
    } finally {
      setActionLoading(null);
    }
  }

  if (status === "loading" || loading) {
    return (
      <div className="flex flex-1 items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-muted border-t-primary" />
      </div>
    );
  }

  if (!isAdmin) {
    return (
      <div className="flex flex-1 flex-col">
        <Navbar />
        <div className="flex flex-1 items-center justify-center">
          <p className="text-lg font-medium text-destructive">Access Denied</p>
        </div>
      </div>
    );
  }

  return (
    <div className="flex flex-1 flex-col">
      <Navbar />
      <main className="flex-1 p-6">
        <div className="mb-4 flex items-center justify-between">
          <h1 className="text-2xl font-bold">All Users</h1>
          <Button onClick={() => { setForm(EMPTY_FORM); setFormError(""); setModalOpen(true); }}>
            Add Employee
          </Button>
        </div>
        {error && <p className="mb-4 text-sm text-destructive">{error}</p>}
        <div className="rounded-md border">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Email</TableHead>
                <TableHead>First Name</TableHead>
                <TableHead>Last Name</TableHead>
                <TableHead>Role</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {users.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={6} className="text-center text-muted-foreground">
                    No users found.
                  </TableCell>
                </TableRow>
              ) : (
                users.map((user) => (
                  <TableRow key={user.id}>
                    <TableCell>{user.email}</TableCell>
                    <TableCell>{user.firstName}</TableCell>
                    <TableCell>{user.lastName}</TableCell>
                    <TableCell>
                      <RoleBadge role={user.role ?? "USER"} />
                    </TableCell>
                    <TableCell>
                      <Badge variant={user.isActive ? "default" : "destructive"}>
                        {user.isActive ? "Active" : "Inactive"}
                      </Badge>
                    </TableCell>
                    <TableCell>
                      {user.isActive ? (
                        <Button
                          size="sm"
                          variant="destructive"
                          disabled={actionLoading === user.id + "_deactivate"}
                          onClick={() => handleDeactivate(user.id)}
                        >
                          {actionLoading === user.id + "_deactivate" ? "..." : "Deactivate"}
                        </Button>
                      ) : (
                        <Button
                          size="sm"
                          variant="outline"
                          disabled={actionLoading === user.id + "_activate"}
                          onClick={() => handleActivate(user.id)}
                        >
                          {actionLoading === user.id + "_activate" ? "..." : "Activate"}
                        </Button>
                      )}
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </div>
      </main>

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Add Employee</DialogTitle>
          </DialogHeader>
          <div className="flex flex-col gap-4 py-2">
            <div className="flex flex-col gap-1">
              <Label htmlFor="firstName">First Name</Label>
              <Input
                id="firstName"
                value={form.firstName}
                onChange={(e) => setForm((f) => ({ ...f, firstName: e.target.value }))}
              />
            </div>
            <div className="flex flex-col gap-1">
              <Label htmlFor="lastName">Last Name</Label>
              <Input
                id="lastName"
                value={form.lastName}
                onChange={(e) => setForm((f) => ({ ...f, lastName: e.target.value }))}
              />
            </div>
            <div className="flex flex-col gap-1">
              <Label htmlFor="email">Email</Label>
              <Input
                id="email"
                type="email"
                value={form.email}
                onChange={(e) => setForm((f) => ({ ...f, email: e.target.value }))}
              />
            </div>
            <div className="flex flex-col gap-1">
              <Label htmlFor="password">Password</Label>
              <Input
                id="password"
                type="password"
                value={form.password}
                onChange={(e) => setForm((f) => ({ ...f, password: e.target.value }))}
              />
            </div>
            <div className="flex flex-col gap-1">
              <Label htmlFor="role">Role</Label>
              <Select
                value={form.role}
                onValueChange={(val) => setForm((f) => ({ ...f, role: val }))}
              >
                <SelectTrigger id="role">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="USER">USER</SelectItem>
                  <SelectItem value="MANAGER">MANAGER</SelectItem>
                  <SelectItem value="ADMIN">ADMIN</SelectItem>
                </SelectContent>
              </Select>
            </div>
            {formError && <p className="text-sm text-destructive">{formError}</p>}
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setModalOpen(false)} disabled={submitting}>
              Cancel
            </Button>
            <Button onClick={handleCreateUser} disabled={submitting}>
              {submitting ? "Creating..." : "Create"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
