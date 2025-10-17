import sys

def find_first_mismatch(file1_path, file2_path):
    with open(file1_path, 'r') as f1, open(file2_path, 'r') as f2:
        for line_number, (line1, line2) in enumerate(zip(f1, f2), start=1):
            if line1 != line2:
                print(f"Mismatch at line {line_number}:")
                print(f"File 1: {line1.rstrip()}")
                print(f"File 2: {line2.rstrip()}")
                return
        # Check if one file has extra lines
        extra1 = f1.readline()
        extra2 = f2.readline()
        if extra1 or extra2:
            print(f"Mismatch: one file has extra lines starting at line {line_number + 1}")
        else:
            print("Files match completely.")

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: python compare_files.py <file1> <file2>")
        sys.exit(1)

    file1 = sys.argv[1]
    file2 = sys.argv[2]
    find_first_mismatch(file1, file2)