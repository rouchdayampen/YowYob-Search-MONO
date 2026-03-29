/**
 * Profile page
 * @author Matteo Owona, Rouchda Yampen
 * @date 2024-12-07
 */

'use client';

import { useSession } from 'next-auth/react';
import { useRouter } from 'next/navigation';
import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import { HeaderAuthenticated } from '@/components/layout/header-authenticated';
import { Footer } from '@/components/layout/footer';
import { Card } from '@/components/ui/card';
import { httpClient } from '@/lib/api/http-client';
import { API_ENDPOINTS } from '@/lib/constants/api-endpoints';
import { toast } from 'sonner';

export default function ProfilePage() {
  const { data: session, status } = useSession();
  const router = useRouter();
  const [profileData, setProfileData] = useState<any>(null);
  const [listingCount, setListingCount] = useState(0);
  const [favoriteCount, setFavoriteCount] = useState(0);
  const [messageCount, setMessageCount] = useState(0);
  const [isEditing, setIsEditing] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    bio: '',
    phoneNumber: '',
    address: '',
    city: ''
  });

  const [passwordData, setPasswordData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  });
  const [isChangingPassword, setIsChangingPassword] = useState(false);

  const accessToken = (session as any)?.user?.accessToken;

  useEffect(() => {
    if (status === 'unauthenticated') {
      router.push('/auth');
    }
  }, [status, router]);

  const fetchData = React.useCallback(async () => {
    if (session?.user?.id && accessToken) {
      try {
        const headers = { Authorization: `Bearer ${accessToken}` };
        const profile = await httpClient.get<any>(API_ENDPOINTS.USER_PROFILE, { headers });
        setProfileData(profile);
        setFormData({
          firstName: profile?.firstName || session.user?.name?.split(' ')[0] || '',
          lastName: profile?.lastName || session.user?.name?.split(' ').slice(1).join(' ') || '',
          bio: profile?.bio || '',
          phoneNumber: profile?.phoneNumber || '',
          address: profile?.address || '',
          city: profile?.city || ''
        });

        const listings = await httpClient.get<any[]>(API_ENDPOINTS.LISTINGS_BY_SELLER(session.user.id), { headers });
        setListingCount(listings?.length || 0);

        try {
          const favorites = await httpClient.get<any[]>(API_ENDPOINTS.USER_FAVORITES, { headers });
          setFavoriteCount(favorites?.length || 0);
        } catch (e) {
          console.error("Favoris indisponibles");
        }

        try {
          const messages = await httpClient.get<any>(`/api/users/messages/count`, { headers });
          setMessageCount(messages?.count || 0);
        } catch (e) {
          console.error("Messages indisponibles");
        }
      } catch (error: any) {
        console.error("Error fetching profile data", error);
        toast.error("Échec du chargement des données. Veuillez vous reconnecter.");
      }
    }
  }, [session, accessToken]);

  useEffect(() => {
    if (session && accessToken) fetchData();
  }, [session, accessToken, fetchData]);

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSaving(true);
    try {
      if (!session?.user) throw new Error("Non authentifié");
      const accessToken = (session as any).user.accessToken as string;
      const userId = (session as any).user?.id || '';
      
      const headers: Record<string, string> = { 
        Authorization: `Bearer ${accessToken}`
      };
      if (userId) {
        headers['X-User-Id'] = userId;
      }
      
      await httpClient.put(API_ENDPOINTS.USER_PROFILE, formData, { headers });
      toast.success("Profil mis à jour avec succès !");
      setIsEditing(false);
      setProfileData({ ...profileData, ...formData });
    } catch (error: any) {
      console.error("Save error details:", error);
      toast.error(`Erreur lors de la sauvegarde : ${error.message || "Serveur inaccessible"}`);
    } finally {
      setIsSaving(false);
    }
  };

  const handlePasswordChange = async (e: React.FormEvent) => {
    e.preventDefault();
    if (passwordData.newPassword !== passwordData.confirmPassword) {
      toast.error("Les mots de passe ne correspondent pas.");
      return;
    }

    setIsChangingPassword(true);
    try {
      if (!session?.user) throw new Error("Non authentifié");
      const accessToken = (session as any).user.accessToken as string;
      const userId = (session as any).user?.id || '';
      
      const headers: Record<string, string> = { 
        Authorization: `Bearer ${accessToken}`
      };
      if (userId) {
        headers['X-User-Id'] = userId;
      }
      
      await httpClient.put('/api/v1/auth/change-password', {
        currentPassword: passwordData.currentPassword,
        newPassword: passwordData.newPassword
      }, { headers });
      
      toast.success("Mot de passe modifié avec succès !");
      setPasswordData({ currentPassword: '', newPassword: '', confirmPassword: '' });
    } catch (error: any) {
      console.error("Password change error:", error);
      toast.error(`Erreur : ${error.message || "Mot de passe actuel incorrect"}`);
    } finally {
      setIsChangingPassword(false);
    }
  };

  if (status === 'loading') {
    return (
      <div className="min-h-screen flex items-center justify-center bg-white dark:bg-gray-900">
        <div className="spinner w-12 h-12"></div>
      </div>
    );
  }

  if (!session) {
    return null;
  }

  const displayName = profileData?.firstName
    ? `${profileData.firstName} ${profileData.lastName || ''}`
    : session.user?.name;

  return (
    <>
      <HeaderAuthenticated userName={session.user?.name || undefined} />
      <div className="min-h-screen p-8 bg-gray-50 dark:bg-gray-900">
        <div className="max-w-4xl mx-auto">
          <div className="flex justify-between items-center mb-8">
            <h1 className="text-3xl font-black gradient-text">
              Mon Profil
            </h1>
            {!isEditing && (
              <button
                onClick={() => setIsEditing(true)}
                className="px-6 py-2 bg-blue-600 hover:bg-blue-700 text-white font-bold rounded-xl transition-all"
              >
                Modifier le profil
              </button>
            )}
          </div>

          <div className="grid gap-8">
            {isEditing ? (
              <>
              <Card className="p-8">
                <form onSubmit={handleSave} className="grid gap-6">
                  <div className="grid md:grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-bold text-gray-700 dark:text-gray-300 mb-2">Prénom</label>
                      <input
                        type="text"
                        value={formData.firstName}
                        onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                        className="w-full p-3 bg-gray-50 dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-xl outline-none focus:border-blue-500"
                        placeholder="Prénom"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-bold text-gray-700 dark:text-gray-300 mb-2">Nom</label>
                      <input
                        type="text"
                        value={formData.lastName}
                        onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                        className="w-full p-3 bg-gray-50 dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-xl outline-none focus:border-blue-500"
                        placeholder="Nom"
                      />
                    </div>
                  </div>

                  <div>
                    <label className="block text-sm font-bold text-gray-700 dark:text-gray-300 mb-2">Bio</label>
                    <textarea
                      value={formData.bio}
                      onChange={(e) => setFormData({ ...formData, bio: e.target.value })}
                      className="w-full p-3 bg-gray-50 dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-xl outline-none focus:border-blue-500"
                      placeholder="Parlez-nous de vous..."
                      rows={3}
                    />
                  </div>

                  <div className="grid md:grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-bold text-gray-700 dark:text-gray-300 mb-2">Téléphone</label>
                      <input
                        type="text"
                        value={formData.phoneNumber}
                        onChange={(e) => setFormData({ ...formData, phoneNumber: e.target.value })}
                        className="w-full p-3 bg-gray-50 dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-xl outline-none focus:border-blue-500"
                        placeholder="Numéro de téléphone"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-bold text-gray-700 dark:text-gray-300 mb-2">Ville</label>
                      <input
                        type="text"
                        value={formData.city}
                        onChange={(e) => setFormData({ ...formData, city: e.target.value })}
                        className="w-full p-3 bg-gray-50 dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-xl outline-none focus:border-blue-500"
                        placeholder="Ville"
                      />
                    </div>
                  </div>

                  <div>
                    <label className="block text-sm font-bold text-gray-700 dark:text-gray-300 mb-2">Adresse</label>
                    <input
                      type="text"
                      value={formData.address}
                      onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                      className="w-full p-3 bg-gray-50 dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-xl outline-none focus:border-blue-500"
                      placeholder="Adresse complète"
                    />
                  </div>

                  <div className="flex justify-end gap-3 mt-4">
                    <button
                      type="button"
                      onClick={() => setIsEditing(false)}
                      className="px-6 py-2 border border-gray-200 dark:border-gray-700 text-gray-600 dark:text-gray-400 font-bold rounded-xl"
                    >
                      Annuler
                    </button>
                    <button
                      type="submit"
                      disabled={isSaving}
                      className="px-8 py-2 bg-blue-600 text-white font-bold rounded-xl shadow-lg shadow-blue-500/30 disabled:opacity-50"
                    >
                      {isSaving ? 'Sauvegarde...' : 'Sauvegarder'}
                    </button>
                  </div>
                </form>
              </Card>

              {/* --- CHANGER DE MOT DE PASSE (visible qu'en mode édition) --- */}
              <Card className="p-8 shadow-sm border-t-4 border-red-500">
                <h3 className="text-xl font-bold mb-6 text-gray-800 dark:text-gray-100 flex items-center">
                  <span className="mr-2">🔐</span> Changer le mot de passe
                </h3>
                <form onSubmit={handlePasswordChange} className="space-y-4">
                  <div className="grid md:grid-cols-2 gap-6 relative">
                    <div className="space-y-2">
                      <label className="text-sm font-semibold text-gray-600 dark:text-gray-300">Mot de passe actuel</label>
                      <input
                        type="password"
                        className="w-full px-4 py-3 rounded-xl border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800 disabled:opacity-50"
                        value={passwordData.currentPassword}
                        onChange={e => setPasswordData({...passwordData, currentPassword: e.target.value})}
                        placeholder="************"
                        required
                      />
                    </div>
                    <div className="space-y-2">
                      <label className="text-sm font-semibold text-gray-600 dark:text-gray-300">Nouveau mot de passe</label>
                      <input
                        type="password"
                        className="w-full px-4 py-3 rounded-xl border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800 disabled:opacity-50"
                        value={passwordData.newPassword}
                        onChange={e => setPasswordData({...passwordData, newPassword: e.target.value})}
                        placeholder="Nouveau mot de passe secret"
                        required
                        minLength={6}
                      />
                    </div>
                    <div className="space-y-2 md:col-start-2">
                      <label className="text-sm font-semibold text-gray-600 dark:text-gray-300">Confirmer le nouveau mot de passe</label>
                      <input
                        type="password"
                        className="w-full px-4 py-3 rounded-xl border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800 disabled:opacity-50"
                        value={passwordData.confirmPassword}
                        onChange={e => setPasswordData({...passwordData, confirmPassword: e.target.value})}
                        placeholder="Confirmer"
                        required
                        minLength={6}
                      />
                    </div>
                  </div>
                  <div className="mt-8 flex justify-end gap-4">
                    <button
                      type="submit"
                      disabled={isChangingPassword}
                      className="px-8 py-3 bg-red-600 hover:bg-red-700 disabled:bg-red-400 text-white font-bold rounded-xl transition-all shadow-md active:scale-95"
                    >
                      {isChangingPassword ? "Modification..." : "Modifier le mot de passe"}
                    </button>
                  </div>
                </form>
              </Card>
              </>
            ) : (
              <>
                <Card className="p-8">
                  <div className="flex items-center gap-8">
                    <div className="w-28 h-28 bg-gradient-to-br from-blue-500 to-cyan-400 rounded-full flex items-center justify-center text-white text-4xl font-black shadow-xl shadow-blue-500/20">
                      {displayName?.[0] || 'U'}
                    </div>
                    <div>
                      <h2 className="text-3xl font-bold text-gray-800 dark:text-gray-100 mb-1">
                        {displayName}
                      </h2>
                      <p className="text-gray-600 dark:text-gray-400 font-medium mb-3">{session.user?.email}</p>

                      <div className="flex flex-wrap gap-3">
                        {profileData?.city && (
                          <span className="px-3 py-1 bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-400 text-xs font-bold rounded-full">
                            📍 {profileData.city}
                          </span>
                        )}
                        {profileData?.phoneNumber && (
                          <span className="px-3 py-1 bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-400 text-xs font-bold rounded-full">
                            📞 {profileData.phoneNumber}
                          </span>
                        )}
                      </div>

                      {profileData?.bio && (
                        <p className="text-gray-600 dark:text-gray-300 mt-4 italic">
                          "{profileData.bio}"
                        </p>
                      )}
                    </div>
                  </div>
                </Card>

                <div className="grid md:grid-cols-3 gap-6">
                  <Link href="/profile/annonces" className="transition-transform hover:-translate-y-1">
                    <Card className="p-6 text-center border-b-4 border-blue-500 h-full">
                      <div className="text-4xl font-black gradient-text mb-2">{listingCount}</div>
                      <p className="text-sm font-bold text-gray-500 uppercase tracking-widest">Annonces</p>
                    </Card>
                  </Link>
                  <Link href="/profile/favoris" className="transition-transform hover:-translate-y-1">
                    <Card className="p-6 text-center border-b-4 border-cyan-400 h-full">
                      <div className="text-4xl font-black gradient-text mb-2">{favoriteCount}</div>
                      <p className="text-sm font-bold text-gray-500 uppercase tracking-widest">Favoris</p>
                    </Card>
                  </Link>
                  <Link href="/profile/messages" className="transition-transform hover:-translate-y-1">
                    <Card className="p-6 text-center border-b-4 border-purple-500 h-full">
                      <div className="text-4xl font-black gradient-text mb-2">{messageCount}</div>
                      <p className="text-sm font-bold text-gray-500 uppercase tracking-widest">Messages</p>
                    </Card>
                  </Link>
                </div>
              </>
            )}
          </div>
        </div>
      </div>
    </>
  );
}