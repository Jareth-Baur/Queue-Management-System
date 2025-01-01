-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Dec 17, 2024 at 06:12 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `queue_management`
--

-- --------------------------------------------------------

--
-- Table structure for table `tickets`
--

CREATE TABLE `tickets` (
  `id` int(11) NOT NULL,
  `ticket_number` varchar(50) NOT NULL,
  `status` enum('PENDING','SERVED') DEFAULT 'PENDING',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `tickets`
--

INSERT INTO `tickets` (`id`, `ticket_number`, `status`, `created_at`) VALUES
(203, 'Ticket-1', 'SERVED', '2024-01-15 04:45:32'),
(204, 'Ticket-2', 'SERVED', '2024-01-20 01:30:15'),
(205, 'Ticket-3', 'SERVED', '2024-01-28 06:12:08'),
(206, 'Ticket-4', 'SERVED', '2024-02-02 09:23:50'),
(207, 'Ticket-5', 'PENDING', '2024-02-10 13:14:18'),
(208, 'Ticket-6', 'PENDING', '2024-02-15 22:32:41'),
(209, 'Ticket-7', 'SERVED', '2024-02-23 05:44:25'),
(210, 'Ticket-8', 'PENDING', '2024-02-29 23:21:10'),
(211, 'Ticket-9', 'SERVED', '2024-03-05 12:05:55'),
(212, 'Ticket-10', 'PENDING', '2024-03-12 02:22:37'),
(213, 'Ticket-11', 'SERVED', '2024-03-15 04:11:04'),
(214, 'Ticket-12', 'PENDING', '2024-03-20 00:35:22'),
(215, 'Ticket-13', 'SERVED', '2024-03-27 08:20:45'),
(216, 'Ticket-14', 'PENDING', '2024-04-02 03:54:13'),
(217, 'Ticket-15', 'SERVED', '2024-04-06 05:40:20'),
(218, 'Ticket-16', 'PENDING', '2024-04-09 21:18:08'),
(219, 'Ticket-17', 'SERVED', '2024-04-15 14:11:12'),
(220, 'Ticket-18', 'PENDING', '2024-04-18 09:50:31'),
(221, 'Ticket-19', 'PENDING', '2024-04-23 02:42:00'),
(222, 'Ticket-20', 'SERVED', '2024-04-30 07:23:14'),
(223, 'Ticket-21', 'PENDING', '2024-04-30 22:40:08'),
(224, 'Ticket-22', 'SERVED', '2024-05-06 10:22:30'),
(225, 'Ticket-23', 'PENDING', '2024-05-09 00:25:11'),
(226, 'Ticket-24', 'PENDING', '2024-05-12 07:11:20'),
(227, 'Ticket-25', 'SERVED', '2024-05-18 03:35:44'),
(228, 'Ticket-26', 'PENDING', '2024-05-22 20:42:55'),
(229, 'Ticket-27', 'SERVED', '2024-05-27 06:10:12'),
(230, 'Ticket-28', 'PENDING', '2024-06-01 04:25:55'),
(231, 'Ticket-29', 'SERVED', '2024-06-03 10:33:22'),
(232, 'Ticket-30', 'PENDING', '2024-06-06 23:51:39'),
(233, 'Ticket-31', 'SERVED', '2024-06-10 13:22:44'),
(234, 'Ticket-32', 'PENDING', '2024-06-12 05:00:00'),
(235, 'Ticket-33', 'SERVED', '2024-06-14 21:42:18'),
(236, 'Ticket-34', 'PENDING', '2024-06-18 02:12:40'),
(237, 'Ticket-35', 'PENDING', '2024-06-20 06:00:12'),
(238, 'Ticket-36', 'SERVED', '2024-06-22 12:35:18'),
(239, 'Ticket-37', 'PENDING', '2024-06-24 03:22:00'),
(240, 'Ticket-38', 'SERVED', '2024-06-25 09:42:10'),
(241, 'Ticket-39', 'PENDING', '2024-06-25 23:15:23'),
(242, 'Ticket-40', 'SERVED', '2024-06-27 07:30:40'),
(243, 'Ticket-41', 'PENDING', '2024-06-28 11:45:05'),
(244, 'Ticket-42', 'PENDING', '2024-06-29 15:50:50'),
(245, 'Ticket-43', 'SERVED', '2023-12-31 16:12:45'),
(246, 'Ticket-44', 'PENDING', '2024-01-04 10:22:00'),
(247, 'Ticket-45', 'SERVED', '2024-01-08 23:30:55'),
(248, 'Ticket-46', 'PENDING', '2024-01-13 14:15:18'),
(249, 'Ticket-47', 'SERVED', '2024-01-19 01:41:25'),
(250, 'Ticket-48', 'PENDING', '2024-01-21 08:30:00'),
(251, 'Ticket-49', 'PENDING', '2024-01-25 04:45:35'),
(252, 'Ticket-50', 'SERVED', '2024-02-01 00:10:55'),
(253, 'Ticket-51', 'PENDING', '2024-02-05 11:55:42'),
(254, 'Ticket-52', 'SERVED', '2024-02-08 07:22:31'),
(255, 'Ticket-53', 'PENDING', '2024-02-13 03:45:08'),
(256, 'Ticket-54', 'SERVED', '2024-02-18 09:10:10'),
(257, 'Ticket-55', 'PENDING', '2024-02-21 20:25:30'),
(258, 'Ticket-56', 'PENDING', '2024-02-28 13:50:15'),
(259, 'Ticket-57', 'SERVED', '2024-03-01 01:12:08'),
(260, 'Ticket-58', 'PENDING', '2024-03-05 03:33:22'),
(261, 'Ticket-59', 'SERVED', '2024-03-08 04:00:00'),
(262, 'Ticket-60', 'PENDING', '2024-03-12 08:15:35'),
(263, 'Ticket-61', 'PENDING', '2024-03-15 14:30:12'),
(264, 'Ticket-62', 'SERVED', '2024-03-20 11:25:00'),
(265, 'Ticket-63', 'PENDING', '2024-03-25 00:10:15'),
(266, 'Ticket-64', 'SERVED', '2024-03-28 06:55:20'),
(267, 'Ticket-65', 'PENDING', '2024-04-01 10:12:00'),
(268, 'Ticket-66', 'PENDING', '2024-04-04 22:50:31'),
(269, 'Ticket-67', 'SERVED', '2024-04-08 01:10:18'),
(270, 'Ticket-68', 'PENDING', '2024-04-12 14:42:50'),
(271, 'Ticket-69', 'SERVED', '2024-04-15 12:10:45'),
(272, 'Ticket-70', 'PENDING', '2024-04-18 10:45:05'),
(273, 'Ticket-71', 'SERVED', '2024-04-20 03:33:22'),
(274, 'Ticket-72', 'PENDING', '2024-04-24 20:12:08'),
(275, 'Ticket-73', 'SERVED', '2024-04-28 07:45:50'),
(276, 'Ticket-74', 'PENDING', '2024-04-30 23:50:31'),
(277, 'Ticket-75', 'PENDING', '2024-05-05 04:20:10'),
(278, 'Ticket-76', 'SERVED', '2024-05-08 09:33:22'),
(279, 'Ticket-77', 'PENDING', '2024-05-12 07:42:08'),
(280, 'Ticket-78', 'SERVED', '2024-05-18 12:50:15'),
(281, 'Ticket-79', 'PENDING', '2024-05-21 06:33:20'),
(282, 'Ticket-80', 'SERVED', '2024-05-27 01:22:00'),
(283, 'Ticket-81', 'PENDING', '2024-06-01 03:00:08'),
(284, 'Ticket-82', 'PENDING', '2024-06-03 06:12:30'),
(285, 'Ticket-83', 'SERVED', '2024-06-07 09:55:50'),
(286, 'Ticket-84', 'PENDING', '2024-06-10 04:45:35'),
(287, 'Ticket-85', 'SERVED', '2024-06-11 23:25:18'),
(288, 'Ticket-86', 'PENDING', '2024-06-14 20:12:00'),
(289, 'Ticket-87', 'PENDING', '2024-06-18 12:30:55'),
(290, 'Ticket-88', 'SERVED', '2024-06-20 11:10:12'),
(291, 'Ticket-89', 'PENDING', '2024-06-22 08:12:50'),
(292, 'Ticket-90', 'SERVED', '2024-06-23 23:55:05'),
(293, 'Ticket-91', 'PENDING', '2024-06-26 04:15:18'),
(294, 'Ticket-92', 'PENDING', '2024-06-27 15:10:31'),
(295, 'Ticket-93', 'SERVED', '2024-06-28 07:25:40'),
(296, 'Ticket-94', 'PENDING', '2024-06-29 10:50:22'),
(297, 'Ticket-95', 'SERVED', '2024-06-30 01:12:00'),
(298, 'Ticket-96', 'PENDING', '2024-06-30 06:22:55'),
(299, 'Ticket-97', 'SERVED', '2024-06-30 10:10:18'),
(300, 'Ticket-98', 'PENDING', '2024-06-30 12:50:31'),
(301, 'Ticket-99', 'SERVED', '2024-06-30 13:45:00'),
(302, 'Ticket-100', 'PENDING', '2024-06-30 15:59:59'),
(303, 'Ticket-303', 'PENDING', '2024-12-17 16:00:25'),
(304, 'Ticket-304', 'PENDING', '2024-12-17 16:09:16'),
(305, 'Ticket-305', 'PENDING', '2024-12-17 16:16:37'),
(306, 'Ticket-306', 'PENDING', '2024-12-17 16:19:05'),
(307, 'Ticket-307', 'PENDING', '2024-12-17 16:19:08'),
(308, 'Ticket-308', 'PENDING', '2024-12-17 16:23:46'),
(309, 'Ticket-309', 'PENDING', '2024-12-17 16:28:12'),
(310, 'Ticket-310', 'PENDING', '2024-12-17 16:29:40'),
(311, 'Ticket-311', 'PENDING', '2024-12-17 16:34:11'),
(312, 'Ticket-312', 'PENDING', '2024-12-17 16:37:19'),
(313, 'Ticket-313', 'PENDING', '2024-12-17 16:41:30'),
(314, 'Ticket-314', 'PENDING', '2024-12-17 16:49:31'),
(315, 'Ticket-315', 'PENDING', '2024-12-17 16:58:23');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `first_name` varchar(50) DEFAULT NULL,
  `last_name` varchar(50) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `username` varchar(50) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `first_name`, `last_name`, `email`, `username`, `password`, `created_at`) VALUES
(1, 'Jareth', 'Baur', 'jarethbaur0223@gmail.com', 'admin', '123', '2024-12-16 06:57:19');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `tickets`
--
ALTER TABLE `tickets`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`),
  ADD UNIQUE KEY `username` (`username`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `tickets`
--
ALTER TABLE `tickets`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=316;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
