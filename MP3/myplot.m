
file = 'oddsMatrix'
extn = '.csv'
c = {'0' '1' '2'}

for i = 0:9
    file_name = strcat(file, num2str(i), extn)
     v = load(file_name);
     v_mat_c1 = reshape(v(:,1), [28, 28]);
     v_mat_c2 = reshape(v(:,2), [28, 28]);
     v_mat_odds = reshape(v(:,3), [28, 28]);

     v_mat_c1 = v_mat_c1';
     v_mat_c2 = v_mat_c2';
     v_mat_odds = v_mat_odds';
     
     for i =  1: 28
         for j = 1:28
             v_mat_odds(i,j) = log(v_mat_odds(i,j));
             v_mat_c1(i,j) = log(v_mat_c1(i,j));
             v_mat_c2(i,j) = log(v_mat_c2(i,j));
         end
     end

     h = figure
     subplot(1,3,1)
     
     image(v_mat_c1,'CDataMapping','scaled')
%      title(strcat('c1',num2str(i)))
     title('c1')
     subplot(1,3,2)
     image(v_mat_c2,'CDataMapping','scaled')
%      title(strcat('c2',num2str(i)))
     title('c2')
     
     subplot(1,3,3)
     image(v_mat_odds,'CDataMapping','scaled')
%      title(strcat('oddsRatio',num2str(i)))
     title('oddsRatio')
     saveas(h, strcat('fig', num2str(i)),'jpg');
    
end




% for i= 1:28
%     for j =1:28
%         if v(i,j) > 1
%             v(i,j) = 1;
%         else
%             v(i,j) = 0;
%         end
%     end
% end

            
%  figure(1)
% %  image(v,'CDataMapping','scaled')
% 
%  
% % caxis([-10,10])
% %  surface(v,color_vec)
% %  figure(2)
%    surf(v)
% %    caxis([-3,1])