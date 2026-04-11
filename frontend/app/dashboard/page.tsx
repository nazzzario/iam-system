"use client";

import { useSession, signOut } from "next-auth/react";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import api from "@/lib/api";
import { hasRole } from "@/lib/jwt";
import { Navbar } from "@/components/navbar";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { StatsCard } from "@/components/stats-card";

interface UserProfile {
  email: string;
  firstName: string;
  lastName: string;
  isActive: boolean;
}

interface AdminStats {
  totalUsers: number;
  activeUsers: number;
  newThisMonth: number;
}

interface Quote {
  content: string;
  author: string;
}

interface ExchangeRate {
  cc: string;
  rate: number;
}

const QUOTES = [
  { content: "The only way to do great work is to love what you do.", author: "Steve Jobs" },
  { content: "Innovation distinguishes between a leader and a follower.", author: "Steve Jobs" },
  { content: "The future belongs to those who believe in the beauty of their dreams.", author: "Eleanor Roosevelt" },
  { content: "It does not matter how slowly you go as long as you do not stop.", author: "Confucius" },
  { content: "Success is not final, failure is not fatal: it is the courage to continue that counts.", author: "Winston Churchill" },
  { content: "The best time to plant a tree was 20 years ago. The second best time is now.", author: "Chinese Proverb" },
  { content: "An investment in knowledge pays the best interest.", author: "Benjamin Franklin" },
  { content: "Strive not to be a success, but rather to be of value.", author: "Albert Einstein" },
];

export default function DashboardPage() {
  const { data: session, status } = useSession();
  const router = useRouter();

  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [profileLoading, setProfileLoading] = useState(true);
  const [editing, setEditing] = useState(false);
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  const [stats, setStats] = useState<AdminStats | null>(null);
  const [statsLoading, setStatsLoading] = useState(false);

  const [quote, setQuote] = useState<Quote | null>(null);
  const [usdRate, setUsdRate] = useState<number | null>(null);
  const [eurRate, setEurRate] = useState<number | null>(null);

  // Derived only after session is authenticated — safe because this renders
  // after the status check below clears the loading spinner.
  const isAdmin = status === "authenticated" && hasRole(session?.accessToken, "ADMIN");
  const isManager = status === "authenticated" && hasRole(session?.accessToken, "MANAGER");

  useEffect(() => {
    if (status === "unauthenticated") router.push("/");
  }, [status, router]);

  // 1. Always fetch profile once authenticated
  useEffect(() => {
    if (status !== "authenticated") return;
    api
      .get<UserProfile>("/api/v1/users/me")
      .then((res) => {
        setProfile(res.data);
        setFirstName(res.data.firstName);
        setLastName(res.data.lastName);
      })
      .catch(() => setError("Failed to load profile."))
      .finally(() => setProfileLoading(false));
  }, [status]);

  // 2. Fetch stats only when isAdmin becomes true
  useEffect(() => {
    if (!isAdmin) return;
    setStatsLoading(true);
    api
      .get<AdminStats>("/api/v1/admin/users/stats")
      .then((res) => setStats(res.data))
      .catch(() => setError("Failed to load stats."))
      .finally(() => setStatsLoading(false));
  }, [isAdmin]);

  // 3. Fetch stats for manager (same endpoint, view-only)
  useEffect(() => {
    if (!isManager || isAdmin) return;
    setStatsLoading(true);
    api
      .get<AdminStats>("/api/v1/admin/users/stats")
      .then((res) => setStats(res.data))
      .catch(() => setError("Failed to load stats."))
      .finally(() => setStatsLoading(false));
  }, [isManager, isAdmin]);

  // 4. Fetch quote & rates only for plain USER (not admin, not manager)
  useEffect(() => {
    if (status !== "authenticated" || isAdmin || isManager) return;

    const randomQuote = QUOTES[Math.floor(Math.random() * QUOTES.length)];
    setQuote(randomQuote);

    fetch("https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json")
      .then((r) => r.json())
      .then((data: ExchangeRate[]) => {
        const usd = data.find((x) => x.cc === "USD");
        const eur = data.find((x) => x.cc === "EUR");
        if (usd) setUsdRate(usd.rate);
        if (eur) setEurRate(eur.rate);
      })
      .catch(() => {});
  }, [status, isAdmin, isManager]);

  async function handleSave() {
    setSaving(true);
    setError("");
    try {
      const res = await api.put<UserProfile>("/api/v1/users/me", { firstName, lastName });
      setProfile(res.data);
      setEditing(false);
    } catch {
      setError("Failed to save changes.");
    } finally {
      setSaving(false);
    }
  }

  // Show spinner while session or profile is loading
  if (status === "loading" || profileLoading) {
    return (
      <div className="flex flex-1 items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-muted border-t-primary" />
      </div>
    );
  }

  if (!profile) {
    return (
      <div className="flex flex-1 flex-col">
        <Navbar />
        <div className="flex flex-1 items-center justify-center text-muted-foreground">
          {error || "No profile data."}
        </div>
      </div>
    );
  }

  // Admin: show spinner until stats arrive
  if (isAdmin) {
    if (statsLoading || !stats) {
      return (
        <div className="flex flex-1 items-center justify-center">
          <div className="h-8 w-8 animate-spin rounded-full border-4 border-muted border-t-primary" />
        </div>
      );
    }

    return (
      <div className="flex flex-1 flex-col">
        <Navbar />
        <main className="flex-1 p-6">
          <h1 className="mb-6 text-2xl font-bold">Admin Dashboard</h1>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
            <StatsCard label="Total Users" value={stats.totalUsers} icon="👥" />
            <StatsCard label="Active Users" value={stats.activeUsers} icon="✅" />
            <StatsCard label="New This Month" value={stats.newThisMonth} icon="📅" />
          </div>
          {error && <p className="mt-4 text-sm text-destructive">{error}</p>}
        </main>
      </div>
    );
  }

  // Manager: show stats with view-only notice
  if (isManager) {
    if (statsLoading || !stats) {
      return (
        <div className="flex flex-1 items-center justify-center">
          <div className="h-8 w-8 animate-spin rounded-full border-4 border-muted border-t-primary" />
        </div>
      );
    }

    return (
      <div className="flex flex-1 flex-col">
        <Navbar />
        <main className="flex-1 p-6">
          <h1 className="mb-6 text-2xl font-bold">Manager Dashboard</h1>
          <div className="mb-6 grid grid-cols-1 gap-4 sm:grid-cols-3">
            <StatsCard label="Total Users" value={stats.totalUsers} icon="👥" />
            <StatsCard label="Active Users" value={stats.activeUsers} icon="✅" />
            <StatsCard label="New This Month" value={stats.newThisMonth} icon="📅" />
          </div>
          <p className="text-sm text-muted-foreground">
            You have view-only access. Contact an administrator to manage users.
          </p>
          {error && <p className="mt-4 text-sm text-destructive">{error}</p>}
        </main>
      </div>
    );
  }

  // USER / MANAGER dashboard
  const initials =
    `${profile.firstName?.[0] ?? ""}${profile.lastName?.[0] ?? ""}`.toUpperCase() ||
    profile.email[0].toUpperCase();

  return (
    <div className="flex flex-1 flex-col">
      <Navbar />
      <main className="flex flex-1 items-start justify-center p-6">
        <div className="flex w-full max-w-md flex-col gap-4">
          <Card>
            <CardHeader className="flex flex-row items-center gap-4">
              <Avatar size="lg">
                <AvatarFallback>{initials}</AvatarFallback>
              </Avatar>
              <div className="flex flex-col gap-1">
                <CardTitle>
                  {profile.firstName} {profile.lastName}
                </CardTitle>
                <span className="text-sm text-muted-foreground">{profile.email}</span>
                <Badge variant={profile.isActive ? "default" : "destructive"}>
                  {profile.isActive ? "Active" : "Inactive"}
                </Badge>
              </div>
            </CardHeader>
            <CardContent className="flex flex-col gap-4">
              {editing ? (
                <>
                  <div className="flex flex-col gap-1">
                    <label className="text-sm font-medium">First Name</label>
                    <input
                      className="rounded-md border border-input bg-background px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                      value={firstName}
                      onChange={(e) => setFirstName(e.target.value)}
                    />
                  </div>
                  <div className="flex flex-col gap-1">
                    <label className="text-sm font-medium">Last Name</label>
                    <input
                      className="rounded-md border border-input bg-background px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                      value={lastName}
                      onChange={(e) => setLastName(e.target.value)}
                    />
                  </div>
                  {error && <p className="text-sm text-destructive">{error}</p>}
                  <div className="flex gap-2">
                    <Button onClick={handleSave} disabled={saving}>
                      {saving ? "Saving…" : "Save"}
                    </Button>
                    <Button variant="outline" onClick={() => setEditing(false)} disabled={saving}>
                      Cancel
                    </Button>
                  </div>
                </>
              ) : (
                <>
                  {error && <p className="text-sm text-destructive">{error}</p>}
                  <div className="flex gap-2">
                    <Button variant="outline" onClick={() => setEditing(true)}>
                      Edit Profile
                    </Button>
                    <Button
                      variant="destructive"
                      onClick={() => signOut({ callbackUrl: "/api/auth/logout" })}
                    >
                      Log Out
                    </Button>
                  </div>
                </>
              )}
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="text-base">Quote of the Day</CardTitle>
            </CardHeader>
            <CardContent>
              {quote ? (
                <>
                  <p className="text-sm italic text-muted-foreground">"{quote.content}"</p>
                  <p className="mt-2 text-sm font-medium">— {quote.author}</p>
                </>
              ) : (
                <p className="text-sm text-muted-foreground">Loading quote...</p>
              )}
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="text-base">Exchange Rates (NBU)</CardTitle>
            </CardHeader>
            <CardContent>
              {usdRate || eurRate ? (
                <div className="flex gap-6">
                  {usdRate && (
                    <div>
                      <p className="text-2xl font-bold">{usdRate.toFixed(2)}</p>
                      <p className="text-sm text-muted-foreground">USD / UAH</p>
                    </div>
                  )}
                  {eurRate && (
                    <div>
                      <p className="text-2xl font-bold">{eurRate.toFixed(2)}</p>
                      <p className="text-sm text-muted-foreground">EUR / UAH</p>
                    </div>
                  )}
                </div>
              ) : (
                <p className="text-sm text-muted-foreground">Loading rates...</p>
              )}
            </CardContent>
          </Card>
        </div>
      </main>
    </div>
  );
}
