package view.profileUI;

import profile.ProfileService;
import profile.SelectedProfileHolder;
import profile.UserProfile;
import java.util.List;

/** Simple adapter using ProfileService e SelectedProfileHolder
 *  The adapter provides access to profile service operations to the UI layer
 */
public class ProfilesAdapter {
    private final ProfileService service;
    public ProfilesAdapter(ProfileService service) { 
        this.service = service; 
    }
    public List<UserProfile> listProfiles() { 
        return service.list(); 
    }
    public boolean createProfile(String nickname, String avatarPath) { 
        return service.create(nickname, avatarPath) != null; 
    }
    public boolean deleteProfile(UserProfile profile) { 
        service.delete(profile.getNickname()); 
        return true; 
    }
    public void selectProfile(UserProfile profile) { 
        SelectedProfileHolder.set(profile); 
    }
}
