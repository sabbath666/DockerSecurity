#!/bin/sh
set -eu

echo "Stopping auditd service (if running)..."

# Stop and disable auditd service (systemd or sysvinit)
if command -v systemctl >/dev/null 2>&1; then
    systemctl stop auditd 2>/dev/null || true
    systemctl disable auditd 2>/dev/null || true
else
    service auditd stop 2>/dev/null || true
    chkconfig auditd off 2>/dev/null || true
fi

echo "Removing auditd packages (best effort)..."

# Remove auditd packages depending on distro
if command -v apt-get >/dev/null 2>&1; then
    apt-get remove --purge -y auditd audispd-plugins || true
    apt-get autoremove -y || true
elif command -v dnf >/dev/null 2>&1; then
    dnf remove -y audit audit-libs audit-libs-python audispd-plugins || true
elif command -v yum >/dev/null 2>&1; then
    yum remove -y audit audit-libs audit-libs-python audispd-plugins || true
elif command -v zypper >/dev/null 2>&1; then
    zypper -n remove audit || true
fi

echo "Removing auditd configuration and log directories..."

# Remove configuration and log directories
rm -rf /etc/audit \
       /var/log/audit \
       /etc/audisp* \
       /etc/sysconfig/auditd 2>/dev/null || true

# Remove leftover rules file locations if they exist
rm -f /etc/audit/audit.rules 2>/dev/null || true
rm -rf /etc/audit/rules.d 2>/dev/null || true

echo "auditd and its configuration/logs have been removed (best effort)."
