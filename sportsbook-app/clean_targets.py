import os
import shutil

def delete_target_folders(root_dir):
    """
    Recursively searches for and deletes all directories named 'target'.
    """
    # Convert to absolute path for clarity
    abs_root = os.path.abspath(root_dir)
    
    if not os.path.exists(abs_root):
        print(f"Error: The directory '{abs_root}' does not exist.")
        return

    print(f"Scanning for 'target' folders in: {abs_root}\n")

    deleted_count = 0

    # os.walk traverses the directory tree
    for dirpath, dirnames, filenames in os.walk(abs_root, topdown=True):
        # Check if 'target' is in the current list of subdirectories
        if 'target' in dirnames:
            target_path = os.path.join(dirpath, 'target')
            
            try:
                # shutil.rmtree deletes a directory and all its contents
                shutil.rmtree(target_path)
                print(f"[DELETED] {target_path}")
                deleted_count += 1
                
                # Critical: Remove 'target' from the list of directories to visit 
                # so os.walk doesn't try to enter the directory we just deleted.
                dirnames.remove('target')
                
            except PermissionError:
                print(f"[ERROR] Permission denied: {target_path} (Is a file open?)")
            except Exception as e:
                print(f"[ERROR] Could not delete {target_path}: {e}")

    print(f"\nCleanup complete. Removed {deleted_count} 'target' folder(s).")

if __name__ == "__main__":
    # Get user input for the path
    user_path = input("Enter the Java project path (Press Enter for current directory): ").strip()
    
    # Default to current directory if input is empty
    target_dir = user_path if user_path else "."
    
    # Safety confirmation
    confirm = input(f"Are you sure you want to recursively delete all 'target' folders in '{os.path.abspath(target_dir)}'? (y/n): ")
    
    if confirm.lower() == 'y':
        delete_target_folders(target_dir)
    else:
        print("Operation cancelled.")
