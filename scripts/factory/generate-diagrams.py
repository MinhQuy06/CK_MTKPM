#!/usr/bin/env python3
"""Factory Method pattern — class, sequence, ERD and DFD (editable, black-and-white draw.io)."""
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / 'common'))
from drawio_kit import (Diagram, Sequence, REPO, DEPENDS, IMPLEMENTS, EXTENDS, AGGREGATES,  # noqa: E402
                        EXTERNAL, PROCESS, STORE)

OUT = REPO / 'docs/factory/diagrams'


def class_diagram():
    d = Diagram('Factory — Class', 2300, 1240, OUT)
    d.title('MẪU FACTORY METHOD — TẠO TÀI KHOẢN THEO VAI TRÒ — SƠ ĐỒ LỚP', 2200)
    d.uml('auth', '«Client»\nAuthServiceImpl', [
        '- accounts: AccountFactories', '+ register(request): UserDto',
        '  factory = accounts.forRole(role)', '    .orElseThrow("Chỉ được đăng ký PATIENT hoặc DOCTOR")',
        '  factory.create(accountRequest(…))'], 40, 110, 560)
    d.uml('admin', '«Client»\nAdminManagementServiceImpl', [
        '- accounts: AccountFactories', '+ createDoctor(request): DoctorDto  (approved mặc định)',
        '+ createPatient(request): PatientDto'], 40, 470, 560)
    d.uml('registry', '«registry»\nAccountFactories', [
        '- byRole: Map<String, AccountFactory>', '+ AccountFactories(factories: List<AccountFactory>)',
        '+ forRole(role): Optional<AccountFactory>', '', 'Không có factory ADMIN → không tự đăng ký Admin'],
          740, 110, 560)
    d.uml('creator', '«abstract · Creator»\nAccountFactory', [
        '- users: UserRepository', '- passwordEncoder: PasswordEncoder', '',
        '+ role(): String  {abstract}', '+ create(r: AccountRequest): ClinicAccount  {final}',
        '  lưu User(email, hash, role(), active)', '  return createProfile(user, r)',
        '# createProfile(user, r): ClinicAccount  {abstract}  ← factory method'], 740, 470, 560)
    d.uml('pfactory', '«ConcreteCreator»\nPatientAccountFactory', [
        '- patients: PatientRepository', '+ role(): "PATIENT"', '# createProfile(user, r): PatientAccount'],
          440, 900, 520)
    d.uml('dfactory', '«ConcreteCreator»\nDoctorAccountFactory', [
        '- doctors, specialties', '+ role(): "DOCTOR"', '# createProfile(user, r): DoctorAccount',
        '  thiếu chuyên khoa → AppException'], 1080, 900, 520)
    d.uml('product', '«sealed interface · Product»\nClinicAccount', ['+ user(): User'], 1500, 110, 520)
    d.uml('pacc', '«record · ConcreteProduct»\nPatientAccount', ['user: User', 'patient: Patient'], 1740, 470, 500)
    d.uml('dacc', '«record · ConcreteProduct»\nDoctorAccount', ['user: User', 'doctor: Doctor'], 1740, 680, 500)
    d.uml('request', '«record»\nAccountRequest', [
        'email (đã chuẩn hóa), rawPassword, fullName, phone,', 'dob, gender, address, medicalHistory,',
        'specialtyId, degree, experienceYears, consultationFee,', 'approved: boolean'], 1700, 940, 560)
    d.edge('auth', 'registry', 'forRole', 'exitX=1;exitY=0.3;entryX=0;entryY=0.3;')
    d.edge('admin', 'registry', 'forRole', 'exitX=1;exitY=0.2;entryX=0;entryY=0.8;')
    d.edge('registry', 'creator', '1..* theo vai trò', AGGREGATES + 'exitX=0.5;exitY=1;entryX=0.5;entryY=0;')
    d.edge('pfactory', 'creator', '', EXTENDS + 'exitX=0.5;exitY=0;entryX=0.25;entryY=1;')
    d.edge('dfactory', 'creator', '', EXTENDS + 'exitX=0.5;exitY=0;entryX=0.75;entryY=1;')
    d.edge('creator', 'product', '«create»', DEPENDS + 'exitX=1;exitY=0.15;entryX=0.2;entryY=1;')
    d.edge('pacc', 'product', '', IMPLEMENTS + 'exitX=0.5;exitY=0;entryX=0.8;entryY=1;')
    d.edge('dacc', 'product', '', IMPLEMENTS + 'exitX=1;exitY=0.5;entryX=1;entryY=0.5;', [(2280, 740), (2280, 150)])
    d.text('note', 'Bước chung (lưu login, mã hóa mật khẩu, đúng vai trò) nằm ở Creator; bước khác nhau theo vai trò (tạo hồ '
           'sơ) là factory method. Đăng ký và Admin tạo tài khoản dùng chung các factory.\nThêm vai trò mới = thêm một '
           'ConcreteCreator + ConcreteProduct; Spring tự đưa vào AccountFactories.', 40, 1150, 2200, 70, 15)
    d.save('factory-class.drawio')


def sequence():
    s = Sequence('Factory — Sequence: bệnh nhân tự đăng ký', [
        'Khách (UI)', 'AuthController', 'AuthServiceImpl\n«Client»', 'AccountFactories', 'PatientAccountFactory\n«ConcreteCreator»',
        'UserRepository', 'PatientRepository'], OUT, col=290)
    s.call(0, 1, 'POST /api/v1/auth/register {role: "PATIENT", …}')
    s.remark('@Valid: role phải khớp ^(PATIENT|DOCTOR)$, email hợp lệ, mật khẩu ≥ 6 ký tự → sai thì 400 ngay tại API.')
    s.call(1, 2, 'register(request)')
    s.step(2, 'Chuẩn hóa email; email / số điện thoại đã tồn tại → AppException 400', 470, 50)
    s.call(2, 3, 'forRole("PATIENT")')
    s.reply(3, 2, 'Optional(PatientAccountFactory)')
    s.begin('alt [không có factory cho vai trò (ADMIN, NURSE…)]')
    s.reply(2, 1, 'AppException("Chỉ được đăng ký tài khoản PATIENT hoặc DOCTOR") → 400')
    s.end()
    s.call(2, 4, 'create(AccountRequest)')
    s.call(4, 5, 'save(User{email, passwordHash, role = role(), active})')
    s.reply(5, 4, 'User')
    s.step(4, 'createProfile(user, request)  ← factory method', 400, 44)
    s.call(4, 6, 'save(Patient{user, fullName, phone, dob, …})')
    s.reply(6, 4, 'Patient')
    s.reply(4, 2, 'PatientAccount(user, patient)')
    s.reply(2, 1, 'UserDto')
    s.reply(1, 0, '201 Created')
    s.footer('Bác sĩ đi cùng đường qua DoctorAccountFactory (hồ sơ chờ duyệt). Admin tạo tài khoản gọi thẳng '
             'forRole("DOCTOR"/"PATIENT"). Mọi bước trong một giao dịch: lỗi ở createProfile thì cả login bị rollback.')
    s.save('factory-sequence.drawio')


def erd():
    d = Diagram('Factory — ERD', 2000, 900, OUT)
    d.title('MẪU FACTORY METHOD — ERD: CÁC BẢNG MỘT TÀI KHOẢN ĐƯỢC TẠO RA', 1900)
    d.table('users', 'users  (Creator ghi)', [
        ('PK', 'id', 'BIGINT'), ('', 'email', 'VARCHAR(255) UK'), ('', 'password_hash', 'VARCHAR(255)'),
        ('', 'role', 'PATIENT | DOCTOR | ADMIN'), ('', 'is_active', 'BOOLEAN'), ('', 'created_at', 'DATETIME')],
            700, 120, 540)
    d.table('patients', 'patients  (PatientAccountFactory ghi)', [
        ('PK,FK', 'user_id', 'BIGINT'), ('', 'full_name', 'VARCHAR(255)'), ('', 'phone', 'VARCHAR(20) UK'),
        ('', 'dob', 'DATE'), ('', 'gender', 'VARCHAR(10)'), ('', 'address', 'VARCHAR(255)'),
        ('', 'medical_history', 'TEXT')], 60, 480, 560)
    d.table('doctors', 'doctors  (DoctorAccountFactory ghi)', [
        ('PK,FK', 'user_id', 'BIGINT'), ('FK', 'specialty_id', 'BIGINT'), ('', 'full_name', 'VARCHAR(255)'),
        ('', 'phone', 'VARCHAR(20) UK'), ('', 'degree', 'VARCHAR(100)'), ('', 'experience_years', 'INT'),
        ('', 'consultation_fee', 'DECIMAL(12,2)'), ('', 'is_approved', 'BOOLEAN')], 760, 480, 560)
    d.table('specialties', 'specialties  (chỉ đọc)', [('PK', 'id', 'BIGINT'), ('', 'name', 'VARCHAR(255)'), ('', '…', '')],
            1460, 520, 460)
    d.rel('patients', 'users', 'user_id (1–1)', 'ERzeroToOne', 'ERmandOne', 'exitX=0.5;exitY=0;entryX=0;entryY=0.5;',
          [(340, 250)])
    d.rel('doctors', 'users', 'user_id (1–1)', 'ERzeroToOne', 'ERmandOne', 'exitX=0.5;exitY=0;entryX=0.5;entryY=1;')
    d.rel('doctors', 'specialties', 'specialty_id', 'ERzeroToMany', 'ERmandOne', 'exitX=1;exitY=0.3;entryX=0;entryY=0.4;')
    d.text('legend', 'Mỗi tài khoản = một dòng users (vai trò do factory quyết định) + đúng một hồ sơ patients HOẶC doctors. '
           'Admin chỉ có qua dữ liệu mẫu, không có factory.\nKý hiệu crow\'s foot: ┤├ đúng một · o┤ không hoặc một · '
           'o< không hoặc nhiều.', 60, 790, 1880, 70, 15)
    d.save('factory-erd.drawio')


def dfd():
    d = Diagram('Factory — DFD', 2100, 900, OUT)
    d.title('MẪU FACTORY METHOD — DFD MỨC 1: MỞ TÀI KHOẢN', 2000)
    d.box('guest', 'Khách\n(tự đăng ký)', 40, 200, 220, 120, EXTERNAL)
    d.box('admin', 'Admin', 40, 460, 220, 120, EXTERNAL)
    d.box('p1', '1.0\nKiểm tra\nđịnh danh', 380, 280, 210, 190, PROCESS)
    d.box('p2', '2.0\nChọn factory\ntheo vai trò', 760, 280, 210, 190, PROCESS)
    d.box('p3', '3.0\nTạo login', 1140, 150, 210, 190, PROCESS)
    d.box('p4', '4.0\nTạo hồ sơ\n(factory method)', 1140, 450, 210, 190, PROCESS)
    d.box('d1', 'D1   users', 1560, 220, 260, 56, STORE)
    d.box('d2', 'D2   patients · doctors', 1560, 520, 330, 56, STORE)
    d.box('d3', 'D3   specialties', 1560, 680, 260, 56, STORE)
    d.edge('guest', 'p1', 'email, mật khẩu, vai trò,\nhồ sơ', 'exitX=1;exitY=0.6;entryX=0;entryY=0.3;')
    d.edge('admin', 'p1', 'tài khoản bác sĩ /\nbệnh nhân', 'exitX=1;exitY=0.4;entryX=0;entryY=0.75;')
    d.edge('d1', 'p1', 'email đã tồn tại?', 'dashed=1;exitX=0.5;exitY=0;entryX=0.5;entryY=0;', [(1690, 120), (485, 120)])
    d.edge('p1', 'p2', 'yêu cầu hợp lệ', 'exitX=1;exitY=0.5;entryX=0;entryY=0.5;')
    d.edge('p2', 'guest', 'vai trò không có factory → 400', 'dashed=1;exitX=0.3;exitY=0.05;entryX=1;entryY=0.2;',
           [(823, 224)])
    d.edge('p2', 'p3', 'AccountRequest', 'exitX=0.9;exitY=0.25;entryX=0;entryY=0.5;')
    d.edge('p3', 'd1', 'User (role)', 'exitX=1;exitY=0.5;entryX=0;entryY=0.5;')
    d.edge('p3', 'p4', 'User đã lưu', 'exitX=0.5;exitY=1;entryX=0.5;entryY=0;')
    d.edge('p4', 'd2', 'Patient / Doctor', 'exitX=1;exitY=0.5;entryX=0;entryY=0.5;')
    d.edge('d3', 'p4', 'chuyên khoa', 'exitX=0;exitY=0.5;entryX=0.8;entryY=0.9;')
    d.text('note', 'Yourdon/DeMarco. 3.0 là bước chung của Creator; 4.0 là factory method khác nhau theo vai trò. '
           'Lỗi ở 4.0 rollback cả 3.0 (cùng giao dịch).', 40, 800, 2000, 50, 14)
    d.save('factory-dfd.drawio')


def use_case():
    d = Diagram('Factory — Use Case', 1800, 1080, OUT)
    d.title('MẪU FACTORY METHOD — MỞ TÀI KHOẢN THEO VAI TRÒ — SƠ ĐỒ USE CASE', 1700)
    d.system('Hệ thống đặt lịch khám — Mở tài khoản (Factory Method)', 280, 80, 1260, 900)
    d.actor('guest', 'Khách', 120, 230)
    d.actor('admin', 'Admin', 120, 660)
    d.case('uc-reg-patient', 'Đăng ký tài khoản\nbệnh nhân', 330, 130)
    d.case('uc-reg-doctor', 'Đăng ký tài khoản bác sĩ', 330, 300)
    d.case('uc-adm-doctor', 'Tạo tài khoản bác sĩ', 330, 560)
    d.case('uc-adm-patient', 'Tạo tài khoản bệnh nhân', 330, 730)
    d.case('uc-open', 'Mở tài khoản theo vai trò\n(AccountFactory)', 780, 430)
    d.case('uc-login', 'Tạo login\n(mã hóa mật khẩu, gán vai trò)', 1170, 300)
    d.case('uc-profile', 'Tạo hồ sơ theo vai trò\n(factory method)', 1170, 560)
    d.case('uc-reject', 'Từ chối vai trò\nkhông có factory', 780, 160)
    d.case('uc-approve', 'Duyệt bác sĩ tự đăng ký', 330, 880)
    d.assoc('guest', 'uc-reg-patient')
    d.assoc('guest', 'uc-reg-doctor')
    d.assoc('admin', 'uc-adm-doctor')
    d.assoc('admin', 'uc-adm-patient')
    d.assoc('admin', 'uc-approve')
    for base in ('uc-reg-patient', 'uc-reg-doctor', 'uc-adm-doctor', 'uc-adm-patient'):
        d.include(base, 'uc-open')
    d.include('uc-open', 'uc-login')
    d.include('uc-open', 'uc-profile')
    d.extend('uc-reject', 'uc-open', 'ADMIN, NURSE…')
    d.text('scope', 'Bốn cách mở tài khoản đều đi qua "Mở tài khoản theo vai trò". Không có factory ADMIN nên không ai '
           'tự đăng ký được Admin. Bác sĩ tự đăng ký chờ Admin duyệt; bác sĩ do Admin tạo được duyệt sẵn.',
           280, 995, 1260, 50, 14)
    d.save('factory-use-case.drawio')


if __name__ == '__main__':
    class_diagram()
    sequence()
    erd()
    dfd()
    use_case()
    print('Generated Factory class, sequence, ERD, DFD and use case in', OUT.relative_to(REPO))
