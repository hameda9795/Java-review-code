"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/lib/store/authStore";
import { adminApi, AdminStatsDTO } from "@/lib/api/admin";
import { Card } from "@/components/ui/card";
import { Users, UserCheck, Star, FileCode, TrendingUp, Calendar, Clock } from "lucide-react";
import { useToast } from "@/components/ui/use-toast";

export default function AdminDashboardPage() {
  const router = useRouter();
  const user = useAuthStore((state) => state.user);
  const { toast } = useToast();
  const [stats, setStats] = useState<AdminStatsDTO | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Check if user is admin
    if (user?.role !== 'ADMIN') {
      toast({
        title: "Access Denied",
        description: "You don't have permission to access this page.",
        variant: "destructive",
      });
      router.push('/dashboard');
      return;
    }

    loadStats();
  }, [user, router, toast]);

  const loadStats = async () => {
    try {
      setLoading(true);
      const data = await adminApi.getAdminStats();
      setStats(data);
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.response?.data?.message || "Failed to load statistics",
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-lg">Loading...</div>
      </div>
    );
  }

  if (!stats) return null;

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-3xl font-bold">Admin Dashboard</h1>
        <p className="text-muted-foreground mt-2">
          Manage users, monitor activity, and view platform statistics
        </p>
      </div>

      {/* User Statistics */}
      <div className="grid gap-4 md:grid-cols-3">
        <Card className="p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-muted-foreground">Total Users</p>
              <h3 className="text-3xl font-bold mt-2">{stats.totalUsers}</h3>
            </div>
            <div className="h-12 w-12 rounded-full bg-blue-100 dark:bg-blue-900 flex items-center justify-center">
              <Users className="h-6 w-6 text-blue-600 dark:text-blue-400" />
            </div>
          </div>
        </Card>

        <Card className="p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-muted-foreground">Active Users</p>
              <h3 className="text-3xl font-bold mt-2">{stats.activeUsers}</h3>
            </div>
            <div className="h-12 w-12 rounded-full bg-green-100 dark:bg-green-900 flex items-center justify-center">
              <UserCheck className="h-6 w-6 text-green-600 dark:text-green-400" />
            </div>
          </div>
        </Card>

        <Card className="p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-muted-foreground">Special Users</p>
              <h3 className="text-3xl font-bold mt-2">{stats.specialUsers}</h3>
            </div>
            <div className="h-12 w-12 rounded-full bg-purple-100 dark:bg-purple-900 flex items-center justify-center">
              <Star className="h-6 w-6 text-purple-600 dark:text-purple-400" />
            </div>
          </div>
        </Card>
      </div>

      {/* Review Statistics */}
      <div>
        <h2 className="text-2xl font-bold mb-4">Review Statistics</h2>
        <div className="grid gap-4 md:grid-cols-4">
          <Card className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">Total Reviews</p>
                <h3 className="text-2xl font-bold mt-2">{stats.totalReviews}</h3>
              </div>
              <FileCode className="h-8 w-8 text-gray-600 dark:text-gray-400" />
            </div>
          </Card>

          <Card className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">Today</p>
                <h3 className="text-2xl font-bold mt-2">{stats.reviewsToday}</h3>
              </div>
              <Clock className="h-8 w-8 text-blue-600 dark:text-blue-400" />
            </div>
          </Card>

          <Card className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">This Week</p>
                <h3 className="text-2xl font-bold mt-2">{stats.reviewsThisWeek}</h3>
              </div>
              <Calendar className="h-8 w-8 text-green-600 dark:text-green-400" />
            </div>
          </Card>

          <Card className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">This Month</p>
                <h3 className="text-2xl font-bold mt-2">{stats.reviewsThisMonth}</h3>
              </div>
              <TrendingUp className="h-8 w-8 text-purple-600 dark:text-purple-400" />
            </div>
          </Card>
        </div>
      </div>

      {/* Quick Actions */}
      <div>
        <h2 className="text-2xl font-bold mb-4">Quick Actions</h2>
        <div className="grid gap-4 md:grid-cols-2">
          <Card
            className="p-6 cursor-pointer hover:bg-muted/50 transition-colors"
            onClick={() => router.push('/dashboard/admin/users')}
          >
            <div className="flex items-center gap-4">
              <Users className="h-10 w-10 text-blue-600" />
              <div>
                <h3 className="text-lg font-semibold">Manage Users</h3>
                <p className="text-sm text-muted-foreground">View and manage all users</p>
              </div>
            </div>
          </Card>

          <Card
            className="p-6 cursor-pointer hover:bg-muted/50 transition-colors"
            onClick={() => router.push('/dashboard/admin/special-users')}
          >
            <div className="flex items-center gap-4">
              <Star className="h-10 w-10 text-purple-600" />
              <div>
                <h3 className="text-lg font-semibold">Special Users</h3>
                <p className="text-sm text-muted-foreground">Create and manage special users</p>
              </div>
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
}
