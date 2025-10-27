"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/lib/store/authStore";
import { adminApi, UserDTO, UpdateUserRequest } from "@/lib/api/admin";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import { useToast } from "@/components/ui/use-toast";
import {
  Edit,
  Trash2,
  RefreshCw,
  Search,
  CheckCircle,
  XCircle,
  Shield,
  User as UserIcon,
} from "lucide-react";
import { formatDistanceToNow } from "date-fns";

export default function UsersManagementPage() {
  const router = useRouter();
  const user = useAuthStore((state) => state.user);
  const { toast } = useToast();
  const [users, setUsers] = useState<UserDTO[]>([]);
  const [filteredUsers, setFilteredUsers] = useState<UserDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");
  const [editingUser, setEditingUser] = useState<UserDTO | null>(null);
  const [editForm, setEditForm] = useState<UpdateUserRequest>({});

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

    loadUsers();
  }, [user, router, toast]);

  useEffect(() => {
    const filtered = users.filter(
      (u) =>
        u.username.toLowerCase().includes(searchTerm.toLowerCase()) ||
        u.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
        (u.fullName && u.fullName.toLowerCase().includes(searchTerm.toLowerCase()))
    );
    setFilteredUsers(filtered);
  }, [searchTerm, users]);

  const loadUsers = async () => {
    try {
      setLoading(true);
      const data = await adminApi.getAllUsers();
      setUsers(data);
      setFilteredUsers(data);
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.response?.data?.message || "Failed to load users",
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  };

  const handleEdit = (user: UserDTO) => {
    setEditingUser(user);
    setEditForm({
      fullName: user.fullName,
      role: user.role,
      subscriptionTier: user.subscriptionTier,
      isActive: user.isActive,
      isSpecialUser: user.isSpecialUser,
      usageLimit: user.usageLimit,
    });
  };

  const handleUpdate = async () => {
    if (!editingUser) return;

    try {
      await adminApi.updateUser(editingUser.id, editForm);
      toast({
        title: "Success",
        description: "User updated successfully",
      });
      setEditingUser(null);
      loadUsers();
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.response?.data?.message || "Failed to update user",
        variant: "destructive",
      });
    }
  };

  const handleDelete = async (userId: string, username: string) => {
    if (!confirm(`Are you sure you want to delete user "${username}"?`)) return;

    try {
      await adminApi.deleteUser(userId);
      toast({
        title: "Success",
        description: "User deleted successfully",
      });
      loadUsers();
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.response?.data?.message || "Failed to delete user",
        variant: "destructive",
      });
    }
  };

  const handleResetUsage = async (userId: string, username: string) => {
    if (!confirm(`Reset usage count for user "${username}"?`)) return;

    try {
      await adminApi.resetUserUsage(userId);
      toast({
        title: "Success",
        description: "User usage reset successfully",
      });
      loadUsers();
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
          <h1 className="text-3xl font-bold">User Management</h1>
          <p className="text-muted-foreground mt-2">
            Manage all users and their permissions
          </p>
        </div>
        <Button onClick={loadUsers}>
          <RefreshCw className="h-4 w-4 mr-2" />
          Refresh
        </Button>
      </div>

      {/* Search */}
      <Card className="p-4">
        <div className="flex items-center gap-2">
          <Search className="h-5 w-5 text-muted-foreground" />
          <Input
            placeholder="Search by username, email, or name..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="flex-1"
          />
        </div>
      </Card>

      {/* Users Table */}
      <Card className="overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-muted/50 border-b">
              <tr>
                <th className="text-left p-4 font-semibold">User</th>
                <th className="text-left p-4 font-semibold">Role</th>
                <th className="text-left p-4 font-semibold">Status</th>
                <th className="text-left p-4 font-semibold">Tier</th>
                <th className="text-left p-4 font-semibold">Reviews</th>
                <th className="text-left p-4 font-semibold">Usage Limit</th>
                <th className="text-left p-4 font-semibold">Last Login</th>
                <th className="text-left p-4 font-semibold">Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredUsers.map((u) => (
                <tr key={u.id} className="border-b hover:bg-muted/30">
                  <td className="p-4">
                    <div>
                      <div className="font-medium flex items-center gap-2">
                        {u.username}
                        {u.isSpecialUser && (
                          <Badge variant="secondary" className="text-xs">
                            Special
                          </Badge>
                        )}
                      </div>
                      <div className="text-sm text-muted-foreground">{u.email}</div>
                      {u.fullName && (
                        <div className="text-xs text-muted-foreground">{u.fullName}</div>
                      )}
                    </div>
                  </td>
                  <td className="p-4">
                    {u.role === 'ADMIN' ? (
                      <Badge variant="default" className="gap-1">
                        <Shield className="h-3 w-3" />
                        Admin
                      </Badge>
                    ) : (
                      <Badge variant="outline" className="gap-1">
                        <UserIcon className="h-3 w-3" />
                        User
                      </Badge>
                    )}
                  </td>
                  <td className="p-4">
                    {u.isActive ? (
                      <Badge variant="default" className="bg-green-600 gap-1">
                        <CheckCircle className="h-3 w-3" />
                        Active
                      </Badge>
                    ) : (
                      <Badge variant="destructive" className="gap-1">
                        <XCircle className="h-3 w-3" />
                        Inactive
                      </Badge>
                    )}
                  </td>
                  <td className="p-4">
                    <Badge
                      variant={u.subscriptionTier === 'PREMIUM' ? 'default' : 'outline'}
                    >
                      {u.subscriptionTier}
                    </Badge>
                  </td>
                  <td className="p-4">
                    <div className="text-sm">
                      <div>{u.reviewsCount} reviews</div>
                      <div className="text-xs text-muted-foreground">
                        {u.totalFilesReviewed} files
                      </div>
                    </div>
                  </td>
                  <td className="p-4">
                    {u.isSpecialUser ? (
                      u.usageLimit ? (
                        <span className="text-sm">{u.usageLimit} reviews</span>
                      ) : (
                        <Badge variant="secondary">Unlimited</Badge>
                      )
                    ) : (
                      <span className="text-sm text-muted-foreground">-</span>
                    )}
                  </td>
                  <td className="p-4">
                    <span className="text-sm text-muted-foreground">
                      {u.lastLoginAt
                        ? formatDistanceToNow(new Date(u.lastLoginAt), {
                            addSuffix: true,
                          })
                        : 'Never'}
                    </span>
                  </td>
                  <td className="p-4">
                    <div className="flex gap-2">
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => handleEdit(u)}
                      >
                        <Edit className="h-4 w-4" />
                      </Button>
                      {u.reviewsCount > 0 && (
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => handleResetUsage(u.id, u.username)}
                        >
                          <RefreshCw className="h-4 w-4" />
                        </Button>
                      )}
                      {u.role !== 'ADMIN' && (
                        <Button
                          size="sm"
                          variant="destructive"
                          onClick={() => handleDelete(u.id, u.username)}
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Card>

      {/* Edit Dialog */}
      {editingUser && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <Card className="w-full max-w-md p-6 space-y-4">
            <h2 className="text-2xl font-bold">Edit User</h2>

            <div className="space-y-4">
              <div>
                <Label>Full Name</Label>
                <Input
                  value={editForm.fullName || ''}
                  onChange={(e) =>
                    setEditForm({ ...editForm, fullName: e.target.value })
                  }
                />
              </div>

              <div>
                <Label>Role</Label>
                <select
                  className="w-full p-2 border rounded"
                  value={editForm.role}
                  onChange={(e) =>
                    setEditForm({ ...editForm, role: e.target.value as any })
                  }
                >
                  <option value="USER">User</option>
                  <option value="ADMIN">Admin</option>
                </select>
              </div>

              <div>
                <Label>Subscription Tier</Label>
                <select
                  className="w-full p-2 border rounded"
                  value={editForm.subscriptionTier}
                  onChange={(e) =>
                    setEditForm({
                      ...editForm,
                      subscriptionTier: e.target.value as any,
                    })
                  }
                >
                  <option value="FREE">Free</option>
                  <option value="PREMIUM">Premium</option>
                </select>
              </div>

              <div className="flex items-center gap-2">
                <input
                  type="checkbox"
                  id="isActive"
                  checked={editForm.isActive}
                  onChange={(e) =>
                    setEditForm({ ...editForm, isActive: e.target.checked })
                  }
                />
                <Label htmlFor="isActive">Active</Label>
              </div>

              <div className="flex items-center gap-2">
                <input
                  type="checkbox"
                  id="isSpecialUser"
                  checked={editForm.isSpecialUser}
                  onChange={(e) =>
                    setEditForm({ ...editForm, isSpecialUser: e.target.checked })
                  }
                />
                <Label htmlFor="isSpecialUser">Special User</Label>
              </div>

              {editForm.isSpecialUser && (
                <div>
                  <Label>Usage Limit (leave empty for unlimited)</Label>
                  <Input
                    type="number"
                    value={editForm.usageLimit || ''}
                    onChange={(e) =>
                      setEditForm({
                        ...editForm,
                        usageLimit: e.target.value ? parseInt(e.target.value) : undefined,
                      })
                    }
                  />
                </div>
              )}
            </div>

            <div className="flex gap-2 justify-end">
              <Button variant="outline" onClick={() => setEditingUser(null)}>
                Cancel
              </Button>
              <Button onClick={handleUpdate}>Save Changes</Button>
            </div>
          </Card>
        </div>
      )}
    </div>
  );
}
