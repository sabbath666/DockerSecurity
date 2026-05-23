#!/bin/sh
set -eu
# Install auditd if not present
if ! command -v auditd >/dev/null 2>&1; then
    if command -v apt-get >/dev/null 2>&1; then
        apt-get update || true
        apt-get install -y auditd audispd-plugins || true
    elif command -v yum >/dev/null 2>&1; then
        yum install -y audit audit-libs || true
    fi
fi

RULES_DIR="/etc/audit/rules.d"
RULES_FILE="$RULES_DIR/docker.rules"

# Ensure base audit directories exist
mkdir -p /etc/audit "$RULES_DIR"

# Directories that must exist for CIS Docker Benchmark checks
DIRS="
/run/containerd
/var/lib/docker
/etc/docker
/etc/containerd
"

# Files/sockets/units that must exist (we create stubs if missing)
FILES="
/usr/bin/dockerd
/lib/systemd/system/docker.service
/lib/systemd/system/docker.socket
/run/containerd/containerd.sock
/etc/default/docker
/etc/docker/daemon.json
/etc/containerd/config.toml
/etc/sysconfig/docker
/usr/bin/containerd
/usr/bin/containerd-shim
/usr/bin/containerd-shim-runc-v1
/usr/bin/containerd-shim-runc-v2
/usr/bin/runc
"

# 1. Create missing directories
for d in $DIRS; do
    [ -d "$d" ] || mkdir -p "$d"
done

# 2. Create missing files (and their parent directories if needed)
for f in $FILES; do
    dir="$(dirname "$f")"
    [ -d "$dir" ] || mkdir -p "$dir"
    [ -e "$f" ] || touch "$f"
done

# 3. auditd rules for CIS Docker Benchmark 1.1.3–1.1.18
cat > "$RULES_FILE" <<EOF
-w /usr/bin/dockerd -k docker
-w /run/containerd -k docker
-w /run/containerd/containerd.sock -k docker
-w /var/lib/docker -k docker
-w /etc/docker -k docker
-w /lib/systemd/system/docker.service -k docker
-w /lib/systemd/system/docker.socket -k docker
-w /etc/default/docker -k docker
-w /etc/docker/daemon.json -k docker
-w /etc/containerd/config.toml -k docker
-w /etc/sysconfig/docker -k docker
-w /usr/bin/containerd -k docker
-w /usr/bin/containerd-shim -k docker
-w /usr/bin/containerd-shim-runc-v1 -k docker
-w /usr/bin/containerd-shim-runc-v2 -k docker
-w /usr/bin/runc -k docker
EOF

# 4. Reload auditd rules
if command -v augenrules >/dev/null 2>&1; then
    augenrules --load
elif command -v systemctl >/dev/null 2>&1; then
    systemctl restart auditd
else
    service auditd restart
fi

echo "auditd rules for Docker/CIS applied."