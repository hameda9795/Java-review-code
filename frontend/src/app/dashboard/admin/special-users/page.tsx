"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/lib/store/authStore";
import { adminApi, UserDTO, CreateSpecialUserRequest } from "@/lib/api/admin";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import { useToast } from "@/components/ui/use-toast";
import { Star, Plus, Trash2, RefreshCw } from "lucide-react";
import { formatDistanceToNow } from "date-fns";

export default function SpecialUsersPage() {
  const router = useRouter();
  const user = useAuthStore((state) => state.user);
  const { toast } = useToast();
  const [specialUsers, setSpecialUsers] = useState<UserDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [createForm, setCreateForm] = useState<CreateSpecialUserRequest>({
    username: '',
    email: '',
    password: '',
    fullName: '',
    usageLimit: undefined,
  });

  useEffect(() => {
    if (user?.role !== 'ADMIN') {
      toast({
        title: "Access Denied",
        description: "You don't have permission to access this page.",
        variant: "destructive",
      });
      router.push('/dashboard');
      return;
    }

    loadSpecialUsers();
  }, [user, router, toast]);

  const loadSpecialUsers = async () => {
    try {
      setLoading(true);
      const data = await adminApi.getSpecialUsers();
      setSpecialUsers(data);
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.response?.data?.message || "Failed to load special users",
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!createForm.username || !createForm.email || !createForm.password) {
      toast({
        title: "Error",
        description: "Please fill in all required fields",
        variant: "destructive",
      });
      return;
    }

    try {
      await adminApi.createSpecialUser(createForm);
      toast({
        title: "Success",
        description: "Special user created successfully",
      });
      setShowCreateForm(false);
      setCreateForm({
        username: '',
        email: '',
        password: '',
        fullName: '',
        usageLimit: undefined,
      });
      loadSpecialUsers();
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.response?.data?.message || "Failed to create special user",
        variant: "destructive",
      });
    }
  };

  const handleDelete = async (userId: string, username: string) => {
    if (!confirm(`Are you sure you want to delete special user "${username}"?`)) return;

    try {
      await adminApi.deleteUser(userId);
      toast({
        title: "Success",
        description: "Special user deleted successfully",
      });
      loadSpecialUsers();
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.response?.data?.message || "Failed to delete user",
        variant: "destructive",
      });
    }
  };

  const handleResetUsage = async (userId: string, username: string) => {
    if (!confirm(`Reset usage count for special user "${username}"?`)) return;

    try {
      await adminApi.resetUserUsage(userId);
      toast({
        title: "Success",
        description: "Usage reset successfully",
      });
      loadSpecialUsers();
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.response?.data?.message || "Failed to reset usage",
        variant: "destructive",
      });
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-lg">Loading...</div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold flex items-center gap-2">
            <Star className="h-8 w-8 text-purple-600" />
            Special Users
          </h1>
          <p className="text-muted-foreground mt-2">
            Create and manage users with custom usage limits
          </p>
        </div>
        <div className="flex gap-2">
          <Button onClick={loadSpecialUsers} variant="outline">
            <RefreshCw className="h-4 w-4 mr-2" />
            Refresh
          </Button>
          <Button onClick={() => setShowCreateForm(true)}>
            <Plus className="h-4 w-4 mr-2" />
            Create Special User
          </Button>
        </div>
      </div>

      {/* Info Card */}
      <Card className="p-4 bg-purple-50 dark:bg-purple-950 border-purple-200 dark:border-purple-800">
        <p className="text-sm text-purple-900 dark:text-purple-100">
          Special users can have custom usage limits that override tier restrictions.
          Leave usage limit empty for unlimited access.
        </p>
      </Card>

      {/* Special Users Grid */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {specialUsers.map((u) => (
          <Card key={u.id} className="p-6 space-y-4">
            <div className="flex items-start justify-between">
              <div className="flex items-center gap-2">
                <div className="h-10 w-10 rounded-full bg-purple-100 dark:bg-purple-900 flex items-center justify-center">
                  <Star className="h-5 w-5 text-purple-600 dark:text-purple-400" />
                </div>
                <div>
                  <h3 className="font-semibold">{u.username}</h3>
                  <p className="text-sm text-muted-foreground">{u.email}</p>
                </div>
              </div>
            </div>

            {u.fullName && (
              <div className="text-sm">
                <span className="text-muted-foreground">Name: </span>
                <span className="font-medium">{u.fullName}</span>
              </div>
            )}

            <div className="space-y-2">
              <div className="flex justify-between text-sm">
                <span className="text-muted-foreground">Usage Limit:</span>
                {u.usageLimit ? (
                  <Badge variant="secondary">{u.usageLimit} reviews</Badge>
                ) : (
                  <Badge variant="default">Unlimited</Badge>
                )}
              </div>

              <div className="flex justify-between text-sm">
                <span className="text-muted-foreground">Current Usage:</span>
                <span className="font-medium">
                  {u.reviewsCount} review{u.reviewsCount !== 1 ? 's' : ''}
                </span>
              </div>

              {u.usageLimit && (
                <div className="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2">
                  <div
                    className="bg-purple-600 h-2 rounded-full"
                    style={{
                      width: `${Math.min((u.reviewsCount / u.usageLimit) * 100, 100)}%`,
                    }}
                  />
                </div>
              )}

              <div className="flex justify-between text-sm">
                <span className="text-muted-foreground">Files Reviewed:</span>
                <span className="font-medium">{u.totalFilesReviewed}</span>
              </div>

              <div className="flex justify-between text-sm">
                <span className="text-muted-foreground">Last Login:</span>
                <span className="text-xs">
                  {u.lastLoginAt
                    ? formatDistanceToNow(new Date(u.lastLoginAt), {
                        addSuffix: true,
                      })
                    : 'Never'}
                </span>
              </div>

              <div className="flex justify-between text-sm">
                <span className="text-muted-foreground">Status:</span>
                <Badge variant={u.isActive ? 'default' : 'destructive'}>
                  {u.isActive ? 'Active' : 'Inactive'}
                </Badge>
              </div>
            </div>

            <div className="flex gap-2 pt-2">
              <Button
                size="sm"
                variant="outline"
                className="flex-1"
                onClick={() => handleResetUsage(u.id, u.username)}
                disabled={u.reviewsCount === 0}
              >
                <RefreshCw className="h-4 w-4 mr-1" />
                Reset
              </Button>
              <Button
                size="sm"
                variant="destructive"
                onClick={() => handleDelete(u.id, u.username)}
              >
                <Trash2 className="h-4 w-4" />
              </Button>
            </div>
          </Card>
        ))}

        {specialUsers.length === 0 && (
          <Card className="col-span-full p-12 text-center">
            <Star className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
            <h3 className="text-lg font-semibold mb-2">No special users yet</h3>
            <p className="text-muted-foreground mb-4">
              Create your first special user to get started
            </p>
            <Button onClick={() => setShowCreateForm(true)}>
              <Plus className="h-4 w-4 mr-2" />
              Create Special User
            </Button>
          </Card>
        )}
      </div>

      {/* Create Form Modal */}
      {showCreateForm && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <Card className="w-full max-w-md p-6">
            <form onSubmit={handleCreate} className="space-y-4">
              <h2 className="text-2xl font-bold flex items-center gap-2">
                <Star className="h-6 w-6 text-purple-600" />
                Create Special User
              </h2>

              <div>
                <Label htmlFor="username">
                  Username <span className="text-red-500">*</span>
                </Label>
                <Input
                  id="username"
                  value={createForm.username}
                  onChange={(e) =>
                    setCreateForm({ ...createForm, username: e.target.value })
                  }
                  required
                />
              </div>

              <div>
                <Label htmlFor="email">
                  Email <span className="text-red-500">*</span>
                </Label>
                <Input
                  id="email"
                  type="email"
                  value={createForm.email}
                  onChange={(e) =>
                    setCreateForm({ ...createForm, email: e.target.value })
                  }
                  required
                />
              </div>

              <div>
                <Label htmlFor="password">
                  Password <span className="text-red-500">*</span>
                </Label>
                <Input
                  id="password"
                  type="password"
                  value={createForm.password}
                  onChange={(e) =>
                    setCreateForm({ ...createForm, password: e.target.value })
                  }
                  required
                />
              </div>

              <div>
                <Label htmlFor="fullName">Full Name</Label>
                <Input
                  id="fullName"
                  value={createForm.fullName}
                  onChange={(e) =>
                    setCreateForm({ ...createForm, fullName: e.target.value })
                  }
                />
              </div>

              <div>
                <Label htmlFor="usageLimit">
                  Usage Limit (leave empty for unlimited)
                </Label>
                <Input
                  id="usageLimit"
                  type="number"
                  min="1"
                  value={createForm.usageLimit || ''}
                  onChange={(e) =>
                    setCreateForm({
                      ...createForm,
                      usageLimit: e.target.value ? parseInt(e.target.value) : undefined,
                    })
                  }
                />
                <p className="text-xs text-muted-foreground mt-1">
                  Number of code reviews this user can perform
                </p>
              </div>

              <div className="flex gap-2 justify-end pt-4">
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => {
                    setShowCreateForm(false);
                    setCreateForm({
                      username: '',
                      email: '',
                      password: '',
                      fullName: '',
                      usageLimit: undefined,
                    });
                  }}
                >
                  Cancel
                </Button>
                <Button type="submit">Create User</Button>
              </div>
            </form>
          </Card>
        </div>
      )}
    </div>
  );
}
